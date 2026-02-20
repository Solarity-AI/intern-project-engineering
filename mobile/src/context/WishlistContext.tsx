// Wishlist Context for managing favorite products
import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect, useRef } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { getWishlist as getWishlistApi, toggleWishlistApi } from '../services/api';

const WISHLIST_STORAGE_KEY = 'wishlist_products';

export interface WishlistItem {
  id: string;
  name: string;
  price?: number;
  imageUrl?: string;
  category?: string;
  averageRating?: number;
  reviewCount?: number;
  addedAt: Date;
}

interface WishlistContextType {
  wishlist: WishlistItem[];
  wishlistCount: number;
  isInWishlist: (productId: string) => boolean;
  addToWishlist: (product: Omit<WishlistItem, 'addedAt'>) => void;
  addMultipleToWishlist: (products: Array<Omit<WishlistItem, 'addedAt'>>) => void;
  removeFromWishlist: (productId: string) => void;
  removeMultipleFromWishlist: (productIds: string[]) => void;
  toggleWishlist: (product: Omit<WishlistItem, 'addedAt'>) => void;
  clearWishlist: () => void;
}

const WishlistContext = createContext<WishlistContextType | undefined>(undefined);

export const WishlistProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [wishlist, setWishlist] = useState<WishlistItem[]>([]);
  // Ref kept in sync with state so callbacks can read current wishlist
  // without being stale closures, and without needing deps in useCallback.
  const wishlistRef = useRef<WishlistItem[]>([]);
  // Tracks IDs that have an in-flight API request and their direction ('add'|'remove'),
  // preventing a rapid double-tap from firing a second toggle (which would undo the first).
  const pendingOps = useRef<Map<string, 'add' | 'remove'>>(new Map());
  // IDs whose in-flight 'add' must be followed by a compensating remove (e.g., clearWishlist
  // was called while the add was still in-flight).
  const compensatingRemoves = useRef<Set<string>>(new Set());
  // Prevents the debounced save from writing before the initial load completes.
  const isInitialized = useRef(false);

  useEffect(() => {
    wishlistRef.current = wishlist;
  }, [wishlist]);

  // Debounced AsyncStorage persistence — coalesces rapid changes into one write.
  useEffect(() => {
    if (!isInitialized.current) return;
    const timer = setTimeout(() => {
      saveWishlistToStorage(wishlist);
    }, 200);
    return () => clearTimeout(timer);
  }, [wishlist]);

  // Load wishlist from AsyncStorage AND Backend on mount
  useEffect(() => {
    loadWishlist();
  }, []);

  const loadWishlist = async () => {
    try {
      // 1. Load local cache (for immediate UI)
      const stored = await AsyncStorage.getItem(WISHLIST_STORAGE_KEY);
      let localItems: WishlistItem[] = [];
      if (stored) {
        const parsed = JSON.parse(stored);
        localItems = parsed.map((item: any) => ({
          ...item,
          addedAt: new Date(item.addedAt),
        }));
        setWishlist(localItems);
      }

      // 2. Sync with Backend (Source of Truth)
      const backendIds = await getWishlistApi();

      // Filter local items to match backend IDs (remove deleted ones)
      // Note: If backend has IDs that local doesn't, we can't show them fully yet
      // because we lack product details. Ideally, we'd fetch details for missing IDs.
      // For now, we assume local cache is mostly up to date or we keep local items if backend fails.

      if (backendIds && backendIds.length > 0) {
        const backendIdSet = new Set(backendIds.map(String));
        const syncedItems = localItems.filter(item => backendIdSet.has(item.id));

        // If backend has more items than local, we might be missing data.
        // In a real app, we would fetch product details for (backendIds - localIds).

        setWishlist(currentWishlist => {
          // Preserve any items added/toggled while getWishlistApi() was in-flight.
          const pendingItems = currentWishlist.filter(
            item => pendingOps.current.has(String(item.id))
          );
          const pendingIds = new Set(pendingItems.map(item => String(item.id)));
          return [
            ...pendingItems,
            ...syncedItems.filter(item => !pendingIds.has(String(item.id))),
          ];
        });
      } else if (backendIds && backendIds.length === 0) {
        // Backend says empty — keep only items whose toggle is still in-flight,
        // as those mutations will land on the backend and are not yet reflected.
        setWishlist(currentWishlist =>
          currentWishlist.filter(item => pendingOps.current.has(String(item.id)))
        );
      }

    } catch (error) {
      console.error('Error syncing wishlist:', error);
    } finally {
      // Allow the debounced save effect to persist future mutations.
      // Explicit storage writes during loading are no longer needed;
      // the effect will flush 200 ms after the last state change above.
      isInitialized.current = true;
    }
  };

  const saveWishlistToStorage = async (items: WishlistItem[]) => {
    try {
      await AsyncStorage.setItem(WISHLIST_STORAGE_KEY, JSON.stringify(items));
    } catch (error) {
      console.error('Error saving wishlist locally:', error);
    }
  };

  const wishlistCount = wishlist?.length || 0;

  const isInWishlist = useCallback(
    (productId: string | number) => {
      const idStr = String(productId);
      return wishlist?.some((item) => String(item.id) === idStr) ?? false;
    },
    [wishlist]
  );

  const addToWishlist = useCallback(
    (product: Omit<WishlistItem, 'addedAt'>) => {
      const idStr = String(product.id);
      // Guard: skip if already in list or if a request for this ID is already in-flight
      // (prevents rapid double-tap from sending a second toggle that would undo the add).
      if (pendingOps.current.has(idStr)) return;
      if (wishlistRef.current.some(item => String(item.id) === idStr)) return;

      pendingOps.current.set(idStr, 'add');

      const newItem: WishlistItem = {
        ...product,
        id: idStr,
        addedAt: new Date(),
      };

      setWishlist(currentWishlist => {
        const list = currentWishlist || [];
        if (list.some(item => String(item.id) === idStr)) {
          return list; // Defensive guard against stale ref
        }
        return [newItem, ...list];
        // Storage is handled by the debounced useEffect — no write here.
      });

      // API call outside the state updater — fires exactly once per gesture.
      toggleWishlistApi(Number(idStr))
        .catch(e => console.error("Backend sync failed", e))
        .finally(() => {
          pendingOps.current.delete(idStr);
          // If clearWishlist was called while this add was in-flight, issue
          // the compensating remove now that the add has landed on the backend.
          if (compensatingRemoves.current.has(idStr)) {
            compensatingRemoves.current.delete(idStr);
            toggleWishlistApi(Number(idStr))
              .catch(e => console.error('Backend sync failed (compensating remove)', e));
          }
        });
    },
    []
  );

  const addMultipleToWishlist = useCallback(
    (products: Array<Omit<WishlistItem, 'addedAt'>>) => {
      const existingIds = new Set(wishlistRef.current.map(item => String(item.id)));
      const uniqueNewProducts = Array.from(new Map(products.map(p => [String(p.id), p])).values());
      // Filter out items already in the list or with an in-flight request
      const toAdd = uniqueNewProducts.filter(p => {
        const id = String(p.id);
        return !existingIds.has(id) && !pendingOps.current.has(id);
      });

      if (toAdd.length === 0) return;

      const newItems = toAdd.map(p => ({
        ...p,
        id: String(p.id),
        addedAt: new Date(),
      }));

      newItems.forEach(item => pendingOps.current.set(item.id, 'add'));

      setWishlist(currentWishlist => {
        const list = currentWishlist || [];
        const currentIds = new Set(list.map(item => String(item.id)));
        const actualNew = newItems.filter(item => !currentIds.has(item.id));
        if (actualNew.length === 0) return list;
        return [...actualNew, ...list];
        // Storage is handled by the debounced useEffect — no write here.
      });

      // API calls outside the state updater — each fires exactly once.
      newItems.forEach(item => {
        const id = item.id;
        toggleWishlistApi(Number(id))
          .catch(e => console.error("Backend sync failed", e))
          .finally(() => {
            pendingOps.current.delete(id);
            if (compensatingRemoves.current.has(id)) {
              compensatingRemoves.current.delete(id);
              toggleWishlistApi(Number(id))
                .catch(e => console.error('Backend sync failed (compensating remove)', e));
            }
          });
      });
    },
    []
  );

  const removeFromWishlist = useCallback(
    (productId: string | number) => {
      const idStr = String(productId);
      // Guard: skip if request already in-flight or item not in the list.
      // pendingOps prevents a rapid double-tap from toggling twice (remove→re-add).
      if (pendingOps.current.has(idStr)) return;
      if (!wishlistRef.current.some(item => String(item.id) === idStr)) return;

      pendingOps.current.set(idStr, 'remove');

      setWishlist(currentWishlist => {
        const list = currentWishlist || [];
        return list.filter((item) => String(item.id) !== idStr);
        // Storage is handled by the debounced useEffect — no write here.
      });

      // API call outside the state updater — fires exactly once per gesture.
      toggleWishlistApi(Number(idStr))
        .catch(e => console.error("Backend sync failed", e))
        .finally(() => pendingOps.current.delete(idStr));
    },
    []
  );

  const removeMultipleFromWishlist = useCallback(
    (productIds: Array<string | number>) => {
      const idsSet = new Set(productIds.map(String));
      // Filter to items that exist and have no in-flight request
      const toRemove = wishlistRef.current.filter(
        item => idsSet.has(String(item.id)) && !pendingOps.current.has(String(item.id))
      );

      if (toRemove.length === 0) return;

      toRemove.forEach(item => pendingOps.current.set(String(item.id), 'remove'));

      const removeSet = new Set(toRemove.map(item => String(item.id)));
      setWishlist(currentWishlist => {
        const list = currentWishlist || [];
        return list.filter((item) => !removeSet.has(String(item.id)));
        // Storage is handled by the debounced useEffect — no write here.
      });

      // API calls outside the state updater — each fires exactly once.
      toRemove.forEach(item => {
        toggleWishlistApi(Number(item.id))
          .catch(e => console.error("Backend sync failed", e))
          .finally(() => pendingOps.current.delete(String(item.id)));
      });
    },
    []
  );

  const toggleWishlist = useCallback(
    (product: Omit<WishlistItem, 'addedAt'>) => {
      if (isInWishlist(product.id)) {
        removeFromWishlist(product.id);
      } else {
        addToWishlist(product);
      }
    },
    [isInWishlist, removeFromWishlist, addToWishlist]
  );

  const clearWishlist = useCallback(() => {
    const currentItems = wishlistRef.current;
    if (currentItems.length === 0) return;

    setWishlist([]);
    // Debounced save useEffect will persist the empty list.

    currentItems.forEach(item => {
      const id = String(item.id);
      const dir = pendingOps.current.get(id);

      if (dir === 'remove') {
        // Already being removed — nothing more to do.
        return;
      }

      if (dir === 'add') {
        // In-flight add: schedule a compensating remove to fire once the add lands.
        // The .finally() in addToWishlist / addMultipleToWishlist checks this set.
        compensatingRemoves.current.add(id);
        return;
      }

      // No pending op — issue an immediate remove.
      pendingOps.current.set(id, 'remove');
      toggleWishlistApi(Number(id))
        .catch(e => console.error('Backend sync failed', e))
        .finally(() => pendingOps.current.delete(id));
    });
  }, []);

  return (
    <WishlistContext.Provider
      value={{
        wishlist,
        wishlistCount,
        isInWishlist,
        addToWishlist,
        addMultipleToWishlist,
        removeFromWishlist,
        removeMultipleFromWishlist,
        toggleWishlist,
        clearWishlist,
      }}
    >
      {children}
    </WishlistContext.Provider>
  );
};

export const useWishlist = (): WishlistContextType => {
  const context = useContext(WishlistContext);
  if (context === undefined) {
    throw new Error('useWishlist must be used within a WishlistProvider');
  }
  return context;
};
