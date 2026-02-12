import * as MockAPI from './MockAPIClient';

describe('MockAPIClient — no network on import', () => {
  it('does not call fetch when the module is imported', () => {
    const fetchSpy = jest.spyOn(global, 'fetch');
    jest.isolateModules(() => {
      require('./MockAPIClient');
    });
    expect(fetchSpy).not.toHaveBeenCalled();
    fetchSpy.mockRestore();
  });
});

const ALL_MOCKS: Array<[string, jest.Mock]> = [
  ['getUserId', MockAPI.getUserId],
  ['getGlobalStats', MockAPI.getGlobalStats],
  ['getProducts', MockAPI.getProducts],
  ['getProduct', MockAPI.getProduct],
  ['getReviews', MockAPI.getReviews],
  ['postReview', MockAPI.postReview],
  ['markReviewAsHelpful', MockAPI.markReviewAsHelpful],
  ['getUserVotedReviews', MockAPI.getUserVotedReviews],
  ['chatWithAI', MockAPI.chatWithAI],
  ['getWishlist', MockAPI.getWishlist],
  ['getWishlistProducts', MockAPI.getWishlistProducts],
  ['toggleWishlistApi', MockAPI.toggleWishlistApi],
  ['getNotifications', MockAPI.getNotifications],
  ['getUnreadCount', MockAPI.getUnreadCount],
  ['markNotificationAsRead', MockAPI.markNotificationAsRead],
  ['markAllNotificationsAsRead', MockAPI.markAllNotificationsAsRead],
  ['createNotification', MockAPI.createNotification],
  ['deleteNotification', MockAPI.deleteNotification],
  ['deleteAllNotifications', MockAPI.deleteAllNotifications],
];

afterEach(() => {
  jest.clearAllMocks();
});

describe('MockAPIClient — structure', () => {
  it.each(ALL_MOCKS)('%s is a jest.fn()', (_name, fn) => {
    expect(jest.isMockFunction(fn)).toBe(true);
  });
});

describe('MockAPIClient — resolve/reject control', () => {
  it('getProduct resolves with configured data', async () => {
    const product = { id: 1, name: 'Widget', description: '', categories: [], price: 9.99 };
    MockAPI.getProduct.mockResolvedValueOnce(product);
    await expect(MockAPI.getProduct(1)).resolves.toEqual(product);
  });

  it('getProduct rejects with configured error', async () => {
    MockAPI.getProduct.mockRejectedValueOnce(new Error('404 Not Found'));
    await expect(MockAPI.getProduct(99)).rejects.toThrow('404 Not Found');
  });

  it('getProducts resolves with a paged result', async () => {
    const page = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: 10,
      last: true,
    };
    MockAPI.getProducts.mockResolvedValueOnce(page);
    await expect(MockAPI.getProducts()).resolves.toEqual(page);
  });

  it('getUserId resolves with a string', async () => {
    MockAPI.getUserId.mockResolvedValueOnce('test-user-id');
    await expect(MockAPI.getUserId()).resolves.toBe('test-user-id');
  });
});

describe('MockAPIClient — call tracking', () => {
  it('tracks call arguments', async () => {
    MockAPI.getProduct.mockResolvedValueOnce({
      id: 42,
      name: 'Tracked',
      description: '',
      categories: [],
      price: 1,
    });
    await MockAPI.getProduct(42);
    expect(MockAPI.getProduct).toHaveBeenCalledTimes(1);
    expect(MockAPI.getProduct).toHaveBeenCalledWith(42);
  });

  it('call count resets after clearAllMocks', () => {
    MockAPI.getProduct.mockResolvedValueOnce({
      id: 1,
      name: 'X',
      description: '',
      categories: [],
      price: 1,
    });
    void MockAPI.getProduct(1);
    jest.clearAllMocks();
    expect(MockAPI.getProduct).toHaveBeenCalledTimes(0);
  });
});
