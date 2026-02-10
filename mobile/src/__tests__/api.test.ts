import { ApiError, getUserMessage, isRetriable } from '../services/api';

// ============================================================
// U23 – Frontend API Error Handling Unit Tests
// ============================================================

describe('ApiError', () => {
  it('should set all fields via constructor', () => {
    const err = new ApiError(404, 'Not Found', 'PRODUCT_NOT_FOUND', 'Product 42 not found', 'id=42');
    expect(err.status).toBe(404);
    expect(err.statusText).toBe('Not Found');
    expect(err.code).toBe('PRODUCT_NOT_FOUND');
    expect(err.serverMessage).toBe('Product 42 not found');
    expect(err.details).toBe('id=42');
  });

  it('should default details to null', () => {
    const err = new ApiError(500, 'Internal Server Error', 'SERVER_ERROR', 'Oops');
    expect(err.details).toBeNull();
  });

  it('should have name "ApiError"', () => {
    const err = new ApiError(400, 'Bad Request', 'VALIDATION', 'bad');
    expect(err.name).toBe('ApiError');
  });

  it('should produce a readable message string', () => {
    const err = new ApiError(422, 'Unprocessable', 'VALIDATION', 'Invalid input');
    expect(err.message).toBe('422 Unprocessable: Invalid input');
  });

  it('should be an instance of Error', () => {
    const err = new ApiError(500, 'ISE', 'ERR', 'msg');
    expect(err).toBeInstanceOf(Error);
  });
});

describe('getUserMessage', () => {
  it('returns user-friendly message for 400', () => {
    expect(getUserMessage(new ApiError(400, '', '', ''))).toContain('Invalid request');
  });

  it('returns user-friendly message for 401', () => {
    expect(getUserMessage(new ApiError(401, '', '', ''))).toContain('sign in');
  });

  it('returns user-friendly message for 403', () => {
    expect(getUserMessage(new ApiError(403, '', '', ''))).toContain('permission');
  });

  it('returns user-friendly message for 404', () => {
    expect(getUserMessage(new ApiError(404, '', '', ''))).toContain('not found');
  });

  it('returns user-friendly message for 409', () => {
    expect(getUserMessage(new ApiError(409, '', '', ''))).toContain('conflicts');
  });

  it('returns user-friendly message for 422', () => {
    expect(getUserMessage(new ApiError(422, '', '', ''))).toContain('invalid');
  });

  it('returns user-friendly message for 429', () => {
    expect(getUserMessage(new ApiError(429, '', '', ''))).toContain('Too many requests');
  });

  it('returns server error message for 500', () => {
    expect(getUserMessage(new ApiError(500, '', '', ''))).toContain('server');
  });

  it('returns server error message for 502', () => {
    expect(getUserMessage(new ApiError(502, '', '', ''))).toContain('server');
  });

  it('returns server error message for 503', () => {
    expect(getUserMessage(new ApiError(503, '', '', ''))).toContain('server');
  });

  it('returns server error message for 504', () => {
    expect(getUserMessage(new ApiError(504, '', '', ''))).toContain('server');
  });

  it('falls back to serverMessage for unknown status', () => {
    expect(getUserMessage(new ApiError(418, '', '', 'I am a teapot'))).toBe('I am a teapot');
  });

  it('falls back to generic message when serverMessage is empty for unknown status', () => {
    expect(getUserMessage(new ApiError(418, '', '', ''))).toBe('Something went wrong. Please try again.');
  });

  it('returns network message for TypeError "Network request failed"', () => {
    expect(getUserMessage(new TypeError('Network request failed'))).toContain('internet connection');
  });

  it('returns timeout message for DOMException AbortError', () => {
    const err = new DOMException('signal is aborted', 'AbortError');
    expect(getUserMessage(err)).toContain('took too long');
  });

  it('returns timeout message for generic AbortError', () => {
    const err = new Error('aborted');
    err.name = 'AbortError';
    expect(getUserMessage(err)).toContain('took too long');
  });

  it('returns network message for "Failed to fetch"', () => {
    expect(getUserMessage(new Error('Failed to fetch'))).toContain('internet connection');
  });

  it('returns generic message for unknown error types', () => {
    expect(getUserMessage('random string')).toBe('Something went wrong. Please try again.');
    expect(getUserMessage(null)).toBe('Something went wrong. Please try again.');
    expect(getUserMessage(undefined)).toBe('Something went wrong. Please try again.');
    expect(getUserMessage(42)).toBe('Something went wrong. Please try again.');
  });
});

describe('isRetriable', () => {
  it.each([408, 429, 500, 502, 503, 504])('returns true for ApiError with status %d', (status) => {
    expect(isRetriable(new ApiError(status, '', '', ''))).toBe(true);
  });

  it.each([400, 401, 403, 404, 409, 422])('returns false for ApiError with status %d', (status) => {
    expect(isRetriable(new ApiError(status, '', '', ''))).toBe(false);
  });

  it('returns true for TypeError (network failure)', () => {
    expect(isRetriable(new TypeError('Network request failed'))).toBe(true);
  });

  it('returns false for AbortError (timeout)', () => {
    const err = new Error('aborted');
    err.name = 'AbortError';
    expect(isRetriable(err)).toBe(false);
  });

  it('returns true for "Failed to fetch" error', () => {
    expect(isRetriable(new Error('Failed to fetch'))).toBe(true);
  });

  it('returns false for generic unknown error', () => {
    expect(isRetriable(new Error('Something else'))).toBe(false);
  });

  it('returns false for non-Error values', () => {
    expect(isRetriable('string')).toBe(false);
    expect(isRetriable(null)).toBe(false);
    expect(isRetriable(undefined)).toBe(false);
  });
});
