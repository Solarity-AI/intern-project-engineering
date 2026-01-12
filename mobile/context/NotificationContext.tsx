// Notification Context for local notification state management
import React, { createContext, useContext, useState, useCallback, ReactNode, useEffect } from 'react';
import { getNotifications, markNotificationAsRead, markAllNotificationsAsRead } from '../services/api';

export type NotificationType = 'review' | 'order' | 'system';

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  body: string;
  timestamp: Date;
  isRead: boolean;
  data?: {
    productId?: string;
    productName?: string;
  };
}

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;
  addNotification: (notification: Omit<Notification, 'id' | 'timestamp' | 'isRead'>) => void;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  clearNotification: (id: string) => void;
  clearAll: () => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const NotificationProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  // Load notifications from Backend on mount
  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      const apiNotifications = await getNotifications();
      const mapped: Notification[] = apiNotifications.map(n => ({
        id: String(n.id),
        type: 'system', // Default type as backend doesn't store type yet
        title: n.title,
        body: n.message,
        timestamp: new Date(n.createdAt),
        isRead: n.isRead,
        data: n.productId ? { productId: String(n.productId) } : undefined
      }));
      setNotifications(mapped);
    } catch (error) {
      console.error('Error loading notifications:', error);
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const addNotification = useCallback(
    (notification: Omit<Notification, 'id' | 'timestamp' | 'isRead'>) => {
      // Optimistic update
      const newNotification: Notification = {
        ...notification,
        id: `local-${Date.now()}`,
        timestamp: new Date(),
        isRead: false,
      };
      setNotifications((prev) => [newNotification, ...prev]);
      
      // Note: We don't have an API to create notifications from client yet
      // (Usually notifications are created by backend events)
    },
    []
  );

  const markAsRead = useCallback((id: string) => {
    // Optimistic update
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
    );
    
    // Sync with Backend
    if (!id.startsWith('local-')) {
      markNotificationAsRead(Number(id)).catch(e => console.error("Backend sync failed", e));
    }
  }, []);

  const markAllAsRead = useCallback(() => {
    // Optimistic update
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    
    // Sync with Backend
    markAllNotificationsAsRead().catch(e => console.error("Backend sync failed", e));
  }, []);

  const clearNotification = useCallback((id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
    // No delete API yet
  }, []);

  const clearAll = useCallback(() => {
    setNotifications([]);
    // No delete all API yet
  }, []);

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        addNotification,
        markAsRead,
        markAllAsRead,
        clearNotification,
        clearAll,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
