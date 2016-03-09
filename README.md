# safepaste

safepaste is a security-conscious paste service for sharing private, encrypted data. All encryption is done client-side and it's impossible for the server, admin, or anyone without your 256 bit key to view the paste. All pastes are encrypted using [AES-256](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard).

Find it online [here](https://safepaste.org).

## Features

* AES-256 with random 256 bit secret keys
* Always over HTTPS
* Optional "burn after reading"
* Always free and open source

Learn more about the service
[here](https://safepaste.org/14e3c3ce#371864573d2a445868316521cc3cc374413f3eb04978281c964527cdc79a64bb).

## Command-line tool

There is a command line tool for uploading and downloading pastes, written in
bash, included in the repo. It supports all of the same options as the web page
and performs the client-side encryption using openssl. To install it, use:

```bash
$ wget https://raw.githubusercontent.com/jeaye/safepaste/master/tool/safepaste
$ chmod +x ./safepaste

# To paste file (assuming safepate script is within PATH):
$ safepaste < my-file

# To paste command output:
$ some-command | safepaste

# To download and decrypt a paste:
$ safepaste https://safepaste.org/f1a8f535#31bcdb56b77528a3c1b540bc460ed07d5b74fcf65eb91733bc4d10884e764caf

# To see more options:
$ safepaste -h
```

## Donate
Feel free to shoot Bitcoins my way: **1HaMvpDjy7QJBDkcZALJr3s26FxLpv5WtJ**

For more information regarding how I use donations, see
[here](http://jeaye.com/donate/).

## License
safepaste is under a strict copyleft license; see the
[LICENSE](https://github.com/jeaye/safepaste/blob/master/LICENSE) file.
