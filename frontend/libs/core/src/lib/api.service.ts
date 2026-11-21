import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { EDUCATED_CONFIG } from './config';

type QueryParams = Record<string, string | number | boolean | null | undefined>;

/** Thin wrapper around HttpClient that prefixes the backend base path. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(EDUCATED_CONFIG).apiUrl;

  get<T>(path: string, query?: QueryParams): Observable<T> {
    return this.http.get<T>(this.baseUrl + path, { params: toParams(query) });
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(this.baseUrl + path, body);
  }

  /** Same as post, but gives the full response (used when the status code matters, e.g. 200 vs 202). */
  postWithResponse<T>(path: string, body: unknown): Observable<HttpResponse<T>> {
    return this.http.post<T>(this.baseUrl + path, body, { observe: 'response' });
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<T>(this.baseUrl + path, body);
  }

  patch<T>(path: string, body: unknown): Observable<T> {
    return this.http.patch<T>(this.baseUrl + path, body);
  }

  /** multipart/form-data upload (files). */
  postForm<T>(path: string, form: FormData): Observable<T> {
    return this.http.post<T>(this.baseUrl + path, form);
  }
}

function toParams(query?: QueryParams): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== null && value !== undefined && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}
