/** @interface */
function CryptoJSInterface(seed) {}

/** @type {!CryptoJSInterface} */
var CryptoJS =
{
  "lib" =
  {
    "WordArray" =
    {
      "prototype" =
      {
        "random" = function(byte_count){},
      },
    },
  },
  "AES" =
  {
    "prototype" =
    {
      "encrypt" = function(msg, key){},
      "decrypt" = function(msg, key){},
    },
  },
  "enc" =
  {
    "Utf8" = {},
  },
  "EvpKDF" =
  {
    "create" = function(options){},
  },
};
