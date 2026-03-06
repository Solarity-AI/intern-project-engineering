// Wishlist Context for managing favorite products
import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect, useRef } from 'react';
import { useAuth } from '@clerk/expo';
import { AuthTokenReadyContext } from './AuthTokenReadyContext';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AppState, AppStateStatus, Platform } from 'react-native';
import { getWishlist as getWishlistApi, toggleWishlistApi, clearWishlistCache } from '../services/api';

const WISHLIST_STORAGE_KEY_PREFIX = 'wishlist_products';
const WISHLIST_SYNC_INTERVAL_MS = 5000;

function getWishlistStorageKey(clerkUserId: string | null | undefined) {
  return clerkUserId ? `${WISHLIST_STORAGE_KEY_PREFIX}:${clerkUserId}` : null;
}

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
  const { isLoaded: isAuthLoaded, isSignedIn, userId: clerkUserId } = useAuth();
  const isTokenProviderReady = useContext(AuthTokenReadyContext);
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
  const lastSignedInStorageKey = useRef<string | null>(getWishlistStorageKey(clerkUserId));
  const wishlistStorageKey = getWishlistStorageKey(clerkUserId);

  useEffect(() => {
    if (wishlistStorageKey) {
      lastSignedInStorageKey.current = wishlistStorageKey;
    }
  }, [wishlistStorageKey]);

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

  const loadWishlist = useCallback(async (options?: { hydrateFromLocalCache?: boolean }) => {
    const hydrateFromLocalCache = options?.hydrateFromLocalCache ?? true;

    try {
      // Force-clear the in-memory API cache so the backend fetch below is never
      // served from a stale entry left over from a previous session or a
      // concurrent client that toggled items independently.
      clearWishlistCache();

      let localItems: WishlistItem[] = wishlistRef.current;

      // 1. Load local cache (for immediate UI) only during initial hydration.
      if (hydrateFromLocalCache && wishlistStorageKey) {
        const stored = await AsyncStorage.getItem(wishlistStorageKey);
        localItems = [];
        if (stored) {
          const parsed = JSON.parse(stored);
          localItems = parsed.map((item: any) => ({
            ...item,
            addedAt: new Date(item.addedAt),
          }));
          setWishlist(localItems);
        }
      }

      // 2. Sync with Backend (Source of Truth)
      const backendIds = await getWishlistApi();
      const localItemMap = new Map(localItems.map(item => [String(item.id), item]));

      if (backendIds && backendIds.length > 0) {
        const syncedItems = backendIds.map((backendId) => {
          const id = String(backendId);
          const existingItem = localItemMap.get(id);

          if (existingItem) {
            return existingItem;
          }

          // Cross-client wishlist membership must still resolve even if this
          // device has never cached the product payload locally.
          return {
            id,
            name: 'Saved product',
            addedAt: new Date(),
          } as WishlistItem;
        });

        setWishlist(currentWishlist => {
          // Preserve any items added/toggled while getWishlistApi() was in-flight.
          const list = currentWishlist || [];
          const pendingItems = list.filter(
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
  }, [wishlistStorageKey]);

  useEffect(() => {
    if (!isAuthLoaded) {
      return;
    }

    if (!isSignedIn) {
      pendingOps.current.clear();
      compensatingRemoves.current.clear();
      wishlistRef.current = [];
      setWishlist([]);
      const storageKeysToRemove = [
        lastSignedInStorageKey.current,
        WISHLIST_STORAGE_KEY_PREFIX,
      ].filter((value): value is string => Boolean(value));
      Promise.all(storageKeysToRemove.map(key => AsyncStorage.removeItem(key))).catch(error => {
        console.error('Error clearing wishlist after sign out:', error);
      });
      isInitialized.current = true;
      return;
    }

    if (!isTokenProviderReady) {
      return;
    }

    isInitialized.current = false;
    loadWishlist({ hydrateFromLocalCache: true });
  }, [isAuthLoaded, isSignedIn, isTokenProviderReady, loadWishlist]);

  useEffect(() => {
    if (!isAuthLoaded || !isSignedIn || !isTokenProviderReady) {
      return;
    }

    const syncFromBackend = () => {
      if (pendingOps.current.size > 0) {
        return;
      }

      loadWishlist({ hydrateFromLocalCache: false }).catch(error => {
        console.error('Error refreshing wishlist from backend:', error);
      });
    };

    const intervalId = setInterval(syncFromBackend, WISHLIST_SYNC_INTERVAL_MS);
    const handleAppStateChange = (nextAppState: AppStateStatus) => {
      if (nextAppState === 'active') {
        syncFromBackend();
      }
    };
    const appStateSubscription = AppState.addEventListener('change', handleAppStateChange);

    let removeWebListeners: (() => void) | undefined;
    if (Platform.OS === 'web' && typeof window !== 'undefined' && typeof document !== 'undefined') {
      const handleWindowFocus = () => syncFromBackend();
      const handleVisibilityChange = () => {
        if (!document.hidden) {
          syncFromBackend();
        }
      };

      window.addEventListener('focus', handleWindowFocus);
      document.addEventListener('visibilitychange', handleVisibilityChange);
      removeWebListeners = () => {
        window.removeEventListener('focus', handleWindowFocus);
        document.removeEventListener('visibilitychange', handleVisibilityChange);
      };
    }

    return () => {
      clearInterval(intervalId);
      appStateSubscription.remove();
      removeWebListeners?.();
    };
  }, [isAuthLoaded, isSignedIn, isTokenProviderReady, loadWishlist]);

  const saveWishlistToStorage = async (items: WishlistItem[]) => {
    if (!isSignedIn || !wishlistStorageKey) {
      return;
    }

    try {
      await AsyncStorage.setItem(wishlistStorageKey, JSON.stringify(items));
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
        .then(() => {
          // Add landed successfully — issue compensating remove only if clearWishlist fired while in-flight.
          if (compensatingRemoves.current.has(idStr)) {
            compensatingRemoves.current.delete(idStr);
            toggleWishlistApi(Number(idStr))
              .catch(e => console.error('Backend sync failed (compensating remove)', e));
          }
        })
        .catch(e => {
          console.error('Backend sync failed', e);
          setWishlist(currentWishlist => (currentWishlist || []).filter(item => String(item.id) !== idStr));
          // Add failed — backend never received the item, so no compensating remove needed.
          compensatingRemoves.current.delete(idStr);
        })
        .finally(() => pendingOps.current.delete(idStr));
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
          .then(() => {
            if (compensatingRemoves.current.has(id)) {
              compensatingRemoves.current.delete(id);
              toggleWishlistApi(Number(id))
                .catch(e => console.error('Backend sync failed (compensating remove)', e));
            }
          })
          .catch(e => {
            console.error('Backend sync failed', e);
            setWishlist(currentWishlist => (currentWishlist || []).filter(entry => String(entry.id) !== id));
            compensatingRemoves.current.delete(id);
          })
          .finally(() => pendingOps.current.delete(id));
      });
    },
    []
  );

  const removeFromWishlist = useCallback(
    (productId: string | number) => {
      const idStr = String(productId);
      const removedItem = wishlistRef.current.find(item => String(item.id) === idStr);
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
        .catch(e => {
          console.error("Backend sync failed", e);
          if (!removedItem) return;
          setWishlist(currentWishlist => {
            const list = currentWishlist || [];
            if (list.some(item => String(item.id) === idStr)) {
              return list;
            }
            return [removedItem, ...list];
          });
        })
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
          .catch(e => {
            console.error("Backend sync failed", e);
            setWishlist(currentWishlist => {
              const list = currentWishlist || [];
              if (list.some(entry => String(entry.id) === String(item.id))) {
                return list;
              }
              return [item, ...list];
            });
          })
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
