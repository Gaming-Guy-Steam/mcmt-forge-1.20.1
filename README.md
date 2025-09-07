# MCMT – Minecraft MultiThreader

**MCMT** is a Forge 1.20.1 mod that forces Minecraft to run tick loops in parallel, with a safe main-thread apply phase for side-effects.  
It aims to keep the main thread as clean as possible, blacklist crash-prone loops, and balance load across all CPU cores.

## Features
- Forced multithreading of chunk and non-chunk ticks
- Automatic blacklisting of crash-prone tick loops (runs them on main thread)
- Side-effect buffering and deterministic application
- Configurable via `/mcmt` commands and config files in `<serverroot>/config/mcmt`
- Built-in logger with CSV export for performance analysis
- English code/comments, EN/NL translations for commands

## Configuration
- `config/mcmt/blacklist.txt` – permanently blacklisted classes
- `config/mcmt/mcmtconfig.toml` – main configuration file

## Commands
- `/mcmt summary` – show current thread usage and stats
- `/mcmt threads current <n>` – set threads for current session
- `/mcmt threads always <n>` – set max threads permanently
- `/mcmt logger start|stop` – control the logger
- `/mcmt create csv` – export CSV log

## License
This project is licensed under the **MCMT Source‑Available License v1.0**.  
You are free to view and study the source code, and to compile and run the mod for personal, non‑commercial use.  
Modification and redistribution are prohibited without prior written permission from the copyright holder.

See the [LICENSE](LICENSE) file for the full license text.
