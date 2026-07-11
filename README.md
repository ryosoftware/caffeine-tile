# Caffeine Tile

Prevents the screen from turning off by temporarily changing the screen timeout.

When enabled from the Quick Settings panel, the app saves your current screen timeout value and restores it when you tap the button again or turn off the screen.

## Features

- Quick Settings tile (Android 7+)
- Follows system dark/light theme and Material You dynamic color accent (Android 12+)
- Recovers caffeine state after device boot or app update
- Foreground service with deactivation notification
- Supports Spanish and English

## Permissions

| Permission | Purpose |
|---|---|
| `WRITE_SETTINGS` | Modify the screen timeout |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Keep the service alive |
| `POST_NOTIFICATIONS` (Android 13+) | Show the deactivation notification |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prevent the service from being killed |
| `RECEIVE_BOOT_COMPLETED` | Restore caffeine state after reboot |

## Certificate

The SHA-256 digest of the signing certificate is consistent across all releases:

```
68bbe6368b9aa8ced2c168abc4b43aa2644c339cf078da96482693cd730406c5
```

Verify with:

```shell
apksigner verify --verbose --print-certs app-release.apk | grep "Signer #1 certificate SHA-256 digest"
```

## License

[CC BY-NC-SA 4.0](LICENSE.md)
