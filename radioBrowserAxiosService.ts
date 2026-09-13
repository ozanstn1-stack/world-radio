/**
 * Radio Browser API - Axios Service with Caching and Pagination
 *
 * Implements:
 * - Mirror failover (de1, nl1, at1)
 * - In-memory LRU / TTL caching to prevent redundant network calls
 * - Parameterized pagination (limit, offset)
 * - User-Agent header compliance per Radio Browser API guidelines
 */

import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';

export interface RadioStation {
  stationuuid: string;
  name: string;
  url: string;
  url_resolved: string;
  homepage: string;
  favicon: string;
  tags: string;
  country: string;
  countrycode: string;
  state: string;
  language: string;
  votes: number;
  codec: string;
  bitrate: number;
  geo_lat: number | null;
  geo_long: number | null;
  clickcount: number;
  lastcheckok: number;
}

export interface StationSearchParams {
  name?: string;
  country?: string;
  countrycode?: string;
  tag?: string;
  language?: string;
  has_geo_info?: boolean;
  is_https?: boolean;
  order?: 'votes' | 'clickcount' | 'name' | 'bitrate' | 'changetimestamp';
  reverse?: boolean;
  hidebroken?: boolean;
  limit?: number;
  offset?: number;
}

export interface PaginatedResult<T> {
  data: T[];
  page: number;
  limit: number;
  hasMore: boolean;
}

interface CacheEntry<T> {
  data: T;
  timestamp: number;
}

class RadioBrowserService {
  private mirrors = [
    'https://de1.api.radio-browser.info',
    'https://nl1.api.radio-browser.info',
    'https://at1.api.radio-browser.info',
    'https://all.api.radio-browser.info'
  ];
  private currentMirrorIndex = 0;
  private client: AxiosInstance;
  private cache = new Map<string, CacheEntry<any>>();
  private defaultTtlMs = 5 * 60 * 1000; // 5 minutes cache TTL

  constructor() {
    this.client = axios.create({
      baseURL: this.mirrors[this.currentMirrorIndex],
      timeout: 12000,
      headers: {
        'User-Agent': 'WorldRadioGlobe/1.0 (info@worldradio.app)',
        'Accept': 'application/json'
      }
    });

    // Mirror failover interceptor
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        if (!originalRequest || originalRequest._retry) {
          return Promise.reject(error);
        }

        // Try next mirror if network error or 5xx server error
        if (!error.response || error.response.status >= 500) {
          originalRequest._retry = true;
          this.currentMirrorIndex = (this.currentMirrorIndex + 1) % this.mirrors.length;
          const newBaseUrl = this.mirrors[this.currentMirrorIndex];
          this.client.defaults.baseURL = newBaseUrl;
          originalRequest.baseURL = newBaseUrl;
          return this.client(originalRequest);
        }

        return Promise.reject(error);
      }
    );
  }

  /**
   * Internal GET request with TTL caching
   */
  private async getWithCache<T>(url: string, config?: AxiosRequestConfig, ttlMs: number = this.defaultTtlMs): Promise<T> {
    const cacheKey = `${url}:${JSON.stringify(config?.params || {})}`;
    const cached = this.cache.get(cacheKey);

    if (cached && (Date.now() - cached.timestamp < ttlMs)) {
      return cached.data as T;
    }

    const response = await this.client.get<T>(url, config);
    this.cache.set(cacheKey, {
      data: response.data,
      timestamp: Date.now()
    });

    // Purge cache if exceeds 250 items (simple LRU eviction)
    if (this.cache.size > 250) {
      const oldestKey = this.cache.keys().next().value;
      if (oldestKey) this.cache.delete(oldestKey);
    }

    return response.data;
  }

  /**
   * Search radio stations with pagination and filtering
   */
  async searchStations(params: StationSearchParams = {}): Promise<PaginatedResult<RadioStation>> {
    const limit = Math.min(params.limit ?? 30, 100);
    const offset = params.offset ?? 0;
    const page = Math.floor(offset / limit) + 1;

    const requestParams = {
      ...params,
      limit,
      offset,
      order: params.order ?? 'votes',
      reverse: params.reverse ?? true,
      hidebroken: params.hidebroken ?? true,
    };

    const data = await this.getWithCache<RadioStation[]>('/json/stations/search', {
      params: requestParams
    });

    return {
      data,
      page,
      limit,
      hasMore: data.length === limit
    };
  }

  /**
   * Fetch stations with coordinates for map / 3D globe display
   */
  async getGlobeStations(limit = 100, offset = 0): Promise<PaginatedResult<RadioStation>> {
    return this.searchStations({
      has_geo_info: true,
      hidebroken: true,
      order: 'votes',
      reverse: true,
      limit,
      offset
    });
  }

  /**
   * Fetch top clicked stations with pagination
   */
  async getTopStations(limit = 50, offset = 0): Promise<PaginatedResult<RadioStation>> {
    const data = await this.getWithCache<RadioStation[]>(`/json/stations/topclick/${limit}`, {
      params: { offset }
    });

    return {
      data,
      page: Math.floor(offset / limit) + 1,
      limit,
      hasMore: data.length === limit
    };
  }

  /**
   * Clear in-memory cache
   */
  clearCache(): void {
    this.cache.clear();
  }
}

export const radioBrowserService = new RadioBrowserService();
export default radioBrowserService;
