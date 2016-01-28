{:foreign-libs [{:file "crypto_js.core.inc.js"
                 :provides ["crypto-js.core"]}
                {:file "crypto_js.md5.inc.js"
                 :provides ["crypto-js.md5"]
                 :requires ["crypto-js.core"]}
                {:file "crypto_js.evpkdf.inc.js"
                 :provides ["crypto-js.evpkdf"]
                 :requires ["crypto-js.core"
                            "crypto-js.md5"]}
                {:file "crypto_js.enc_base64.inc.js"
                 :provides ["crypto-js.enc-base64"]
                 :requires ["crypto-js.core"]}
                {:file "crypto_js.cipher_core.inc.js"
                 :provides ["crypto-js.cipher-core"]
                 :requires ["crypto-js.core"
                            "crypto-js.evpkdf"
                            "crypto-js.enc-base64"]}
                {:file "crypto_js.aes.inc.js"
                 :provides ["crypto-js.aes"]
                 :requires ["crypto-js.core"
                            "crypto-js.cipher-core"]}]
:externs ["crypto_js.ext.js"]}
