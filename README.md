Snitchcord
----------
Send Snitch alerts to Discord using Webhooks

### How to Use

1. install the [latest `.jar`](https://github.com/Gjum/Snitchcord/releases) (uses Forge)
1. create a webhook in the config screen of one of your Discord channels (or ask an admin to create a webhook url for you)
    - click the gear next to the channel name
    - select `Webhooks` on the left
    - click `Create Webhook`
    - copy the `Webhook Url` at the bottom
1. open the config screen:
    - from the start screen: `Mods`, select Snitchcord on the left, then click `Config` at the bottom left
    - from the escape menu: `Mods config`, select Snitchcord on the left, then click `Config` at the bottom left
1. paste the webhook url, change other options if you like
1. click `Done` so your changes are saved (don't hit Escape)

![config options screenshot of v2.0.0](https://i.imgur.com/drkWf2k.jpg)

### Alert Formatting

The `{"content":"..."}` is necessary to tell Discord this is a text message.
You can change it to something else if you know the format.
<!-- TODO link to discord docs -->

All the `<...>` will be replaced by various alert information:

| format key        | replacement examples       |
|:------------------|:---------------------------|
| \<player\>        | Gjum                       |
| \<snitch\>        | MySnitch                   |
| \<longAction\>    | entered snitch at \| ...   |
| \<shortAction\>   | enter \| login \| logout   |
| \<nonEnter\>      | login \| logout            |
| \<enter\>         | enter                      |
| \<login\>         | login                      |
| \<logout\>        | logout                     |
| \<world\>         | World \| Nether \| The End |
| \<nonWorld\>      | Nether \| The End          |
| \<coords\>        | -1234 56 -789              |
| \<x\>             | -1234                      |
| \<y\>             | 56                         |
| \<z\>             | -789                       |
| \<roundedCoords\> | -1230 60 -790              |
| \<rx\>            | -1230                      |
| \<ry\>            | 60                         |
| \<rz\>            | -790                       |

Standard world names are replaced with their friendlier variants (see table).

You can put colons around optional format keys (`nonEnter`, `nonWorld`) that you'd like to be formatted only when they are shown.

For example, upon a snitch entry event, the output for the alert format

`{"content":"<player> **<nonEnter>** at <snitch>"}`

would be `PLAYER ** at SNITCHNAME`, which probably isn't what you want.

Instead, you can do

`{"content":"<player> <**:nonEnter:**> at <snitch>"}`

which would result in `PLAYER at SNITCHNAME`.

[![Travis build Status](https://travis-ci.org/Gjum/Snitchcord.svg?branch=master)](https://travis-ci.org/Gjum/Snitchcord)

