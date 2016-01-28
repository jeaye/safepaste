/** @interface */
function CryptoJSInterface(seed) {}

/** @type {!CryptoJSInterface} */
var CryptoJS =
{
  "lib" =
  {
    "WordArray" = {}
  }
};

CryptoJS.WordArray.prototype.random = function (bytes){};
