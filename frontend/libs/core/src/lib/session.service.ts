import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { firstValueFrom } from 'rxjs';
import { EDUCATED_CONFIG } from './config';
import { EstablishmentStore } from './establishment-store';
import { AppModule, Establishment, Me, Operation } from './models';
import { errorMessage } from './problem';

export type SessionStatus = 'loading' | 'ready' | 'error';

/** Signs the user in with Keycloak and keeps their profile and rights (from /api/me). */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly oauth = inject(OAuthService);
  private readonly http = inject(HttpClient);
  private readonly config = inject(EDUCATED_CONFIG);
  private readonly establishmentStore = inject(EstablishmentStore);

  readonly status = signal<SessionStatus>('loading');
  readonly error = signal<string | null>(null);
  readonly me = signal<Me | null>(null);
  readonly establishments = signal<Establishment[]>([]);
  readonly selectedEstablishmentId = this.establishmentStore.selected;

  readonly displayName = computed(() => {
    const me = this.me();
    if (!me) {
      return '';
    }
    const name = [me.firstName, me.lastName].filter(Boolean).join(' ');
    return name || me.username;
  });

  /** Called once when the app starts. Redirects to Keycloak if the user is not signed in yet. */
  async start(): Promise<void> {
    this.oauth.configure({
      issuer: this.config.issuer,
      clientId: this.config.clientId,
      redirectUri: window.location.origin + '/',
      postLogoutRedirectUri: window.location.origin + '/',
      responseType: 'code',
      scope: 'openid profile email',
      requireHttps: 'remoteOnly',
    });
    try {
      await this.oauth.loadDiscoveryDocumentAndLogin();
    } catch {
      this.fail('Connexion impossible au serveur d’authentification (Keycloak).');
      return;
    }
    if (!this.oauth.hasValidAccessToken()) {
      return; // the browser is on its way to the Keycloak sign-in page
    }
    this.oauth.setupAutomaticSilentRefresh();
    try {
      await this.loadProfile();
      this.status.set('ready');
    } catch (error) {
      this.fail(errorMessage(error));
    }
  }

  private fail(message: string): void {
    this.error.set(message);
    this.status.set('error');
  }

  /** Loads /api/me and the establishments the user may switch to. */
  async loadProfile(): Promise<void> {
    let me: Me;
    try {
      me = await firstValueFrom(this.http.get<Me>(`${this.config.apiUrl}/me`));
    } catch (error) {
      // A remembered establishment the user no longer has access to: forget it and try again.
      if (error instanceof HttpErrorResponse && error.status === 403 && this.establishmentStore.selected()) {
        this.establishmentStore.select(null);
        me = await firstValueFrom(this.http.get<Me>(`${this.config.apiUrl}/me`));
      } else {
        throw error;
      }
    }
    this.me.set(me);
    const list = await firstValueFrom(
      this.http.get<Establishment[]>(`${this.config.apiUrl}/parametrage/establishments`),
    );
    this.establishments.set(list);
    if (!this.establishmentStore.selected() && me.establishmentId) {
      this.establishmentStore.select(me.establishmentId);
    }
  }

  async switchEstablishment(id: string): Promise<void> {
    this.establishmentStore.select(id);
    await this.loadProfile();
  }

  /** Mirrors the backend rule: administrative users may do everything. The backend still checks every call. */
  can(entityType: string, operation: Operation): boolean {
    const me = this.me();
    return !!me && (me.administrative || (me.permissions[entityType] ?? []).includes(operation));
  }

  isModuleVisible(module: AppModule): boolean {
    const me = this.me();
    return !!me && (me.administrative || me.visibleModules.includes(module));
  }

  logout(): void {
    this.establishmentStore.select(null);
    this.oauth.logOut();
  }
}
