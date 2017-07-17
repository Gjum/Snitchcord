Snitchcord
----------
Send Snitch alerts to Discord using Webhooks

### How to Use

1. Install the [latest `.jar`](https://github.com/Gjum/Snitchcord/releases) (uses Forge)
1. Create a webhook in the config screen of one of your Discord channels (or ask an admin to create a webhook url for you)
    - Click the gear next to the channel name
    - Select `Webhooks` on the left
    - Click `Create Webhook`
    - Copy the `Webhook Url` at the bottom
1. Open the config screen:
    - From the start screen: `Mods`, select Snitchcord on the left, then click `Config` at the bottom left
    - From the Escape menu: `Mods config`, select Snitchcord on the left, then click `Config` at the bottom left
1. Paste the webhook url, change other options if you like
1. Click `Done` so your changes are saved (don't hit Escape)

![config options screenshot of v2.0.0](https://i.imgur.com/drkWf2k.jpg)

### Alert Formatting

The `{"content":"..."}` is necessary to tell Discord this is a text message.
You can change it to something else if you know the format.
<!-- TODO link to discord docs -->

All the `<...>` will be replaced by various alert information:

| format key        | replacement examples       | optional? |
|:------------------|:---------------------------|:----------|
| \<time\>          | 05:23                      |           |
| \<timeUTC\>       | 00:23                      |           |
| \<player\>        | Gjum                       |           |
| \<snitch\>        | MySnitch                   |     ✔    |
| \<longAction\>    | entered snitch at \| ...   |           |
| \<shortAction\>   | Enter \| Login \| Logout   |           |
| \<nonEnter\>      | Login \| Logout            |     ✔    |
| \<enter\>         | Enter                      |     ✔    |
| \<login\>         | Login                      |     ✔    |
| \<logout\>        | Logout                     |     ✔    |
| \<world\>         | World \| Nether \| The End |           |
| \<nonWorld\>      | Nether \| The End          |     ✔    |
| \<coords\>        | -1234 56 -789              |           |
| \<x\>             | -1234                      |           |
| \<y\>             | 56                         |           |
| \<z\>             | -789                       |           |
| \<roundedCoords\> | -1230 60 -790              |           |
| \<rx\>            | -1230                      |           |
| \<ry\>            | 60                         |           |
| \<rz\>            | -790                       |           |

Standard world names are replaced with their friendlier variants (see table).

You can put colons around optional format keys (`nonEnter`, `nonWorld`, etc.) that you'd like to be formatted only when they are shown.

For example, upon a snitch entry event, the output for alert format

`{"content":"<player> @<nonEnter>@ at <snitch>"}`

would be `PLAYER @@ at SNITCHNAME`, which probably isn't what you want.

Instead, you can do

`{"content":"<player>< @:nonEnter:@> at <snitch>"}`

which would result in `PLAYER at SNITCHNAME`.

[![Travis build Status](https://travis-ci.org/Gjum/Snitchcord.svg?branch=master)](https://travis-ci.org/Gjum/Snitchcord)

