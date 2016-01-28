{:foreign-libs [{:file "src/js/crypto_js.core.inc.js"
                 :provides ["crypto-js.core"]}
                {:file "src/js/crypto_js.md5.inc.js"
                 :provides ["crypto-js.md5"]
                 :requires ["crypto-js.core"]}
                {:file "src/js/crypto_js.evpkdf.inc.js"
                 :provides ["crypto-js.evpkdf"]
                 :requires ["crypto-js.core"
                            "crypto-js.md5"]}
                {:file "src/js/crypto_js.enc_base64.inc.js"
                 :provides ["crypto-js.enc-base64"]
                 :requires ["crypto-js.core"]}
                {:file "src/js/crypto_js.cipher_core.inc.js"
                 :provides ["crypto-js.cipher-core"]
                 :requires ["crypto-js.core"
                            "crypto-js.evpkdf"
                            "crypto-js.enc-base64"]}
                {:file "src/js/crypto_js.aes.inc.js"
                 :provides ["crypto-js.aes"]
                 :requires ["crypto-js.core"
                            "crypto-js.cipher-core"]}]
:externs ["src/js/crypto_js.ext.js"]}
