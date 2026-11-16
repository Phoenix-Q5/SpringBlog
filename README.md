# Spring Blog

## Run with shared Docker infrastructure

Start the infrastructure stack first. It must provide PostgreSQL on the external
`infra` network, Traefik on the external `proxy` network, and a `blog` PostgreSQL
database accessible to the configured database user. If necessary, create the
database by connecting to the existing `postgres` database and running
`CREATE DATABASE blog;`.

1. Copy `.env.example` to `.env`. Set the PostgreSQL credentials to match the
   shared server and set `SPRING_MAIL_USERNAME` and `SPRING_MAIL_PASSWORD` to a
   Gmail address and app password.
2. Set `BLOG_HOST` to the public hostname and `APP_PUBLIC_BASE_URL` to its
   public HTTPS URL. The latter is used for account-verification emails.
3. Run `docker compose up -d --build`, then check `docker compose logs -f blog`.

The app is reachable through Traefik at `https://api-blog.corevanta.app` by
default; Compose does not publish an application port on the host. Configure
the Cloudflare tunnel's public hostname to forward to `http://traefik:80` on
the shared proxy network. The hostname must match `BLOG_HOST`.

The repository currently includes the JWT signing keystore and its password.
Replace it with a private, externally managed signing key before exposing this
application publicly; otherwise anyone with a copy can forge authentication tokens.

`SPRING_JPA_HIBERNATE_DDL_AUTO` defaults to `update`. Avoid `create` on a
database with data to preserve. The container uses `docker/application.properties`,
not the local IntelliJ configuration, so local database and mail credentials
are not included in the Docker build.

For local IntelliJ runs, `APP_PUBLIC_BASE_URL` defaults to
`http://localhost:<server.port>` and can be overridden in the run configuration.
