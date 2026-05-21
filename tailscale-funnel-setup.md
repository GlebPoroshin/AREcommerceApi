# Tailscale 
 — публикация локального сервиса в интернет

Публикует локальный сервис (например, Docker-контейнер на макбуке) в интернет по постоянному HTTPS-URL вида `https://macbook.tail-scale-xxxx.ts.net`. Полностью бесплатно для личного использования, URL не меняется.

## Как работает

Ставишь Tailscale на мак → он даёт устройству имя в твоей tailnet → Funnel выставляет выбранный порт наружу через инфраструктуру Tailscale. TLS-сертификат от Let's Encrypt выпускается автоматически.

## Настройка

### 1. Установка

```bash
brew install tailscale
sudo tailscale up
# откроется браузер для логина (Google/GitHub/email)
```

### 2. Включить Funnel в админке (один раз)

- Зайди на https://login.tailscale.com/admin/dns → включи **MagicDNS** и **HTTPS Certificates**
- https://login.tailscale.com/admin/acls → в ACL добавь:

```json
{
  "nodeAttrs": [
    {"target": ["autogroup:member"], "attr": ["funnel"]}
  ]
}
```

### 3. Запустить Funnel

Контейнер слушает, например, `localhost:8080`:

```bash
sudo tailscale funnel 8080
```

В выводе будет URL вида `https://macbook.tail-xxxx.ts.net` — он постоянный.

## Ограничения

- Только порты **443, 8443, 10000** наружу (внутри проксирует на любой локальный порт — это ок).
- 3 устройства с Funnel на бесплатном плане.
- Имя хоста = имя устройства в tailnet (можно переименовать в админке).
- URL длинный и некрасивый — но постоянный и бесплатный.

## Запуск в фоне после перезагрузки

```bash
sudo tailscale funnel --bg 8080
```

Флаг `--bg` сохраняет конфигурацию, Funnel сам поднимется при старте Tailscale.

## Остановить

```bash
sudo tailscale funnel --https=443 off
```
