#!/bin/bash # -*- mode: Clojure; -*-
#_(

   #_DEPS is same format as deps.edn. Multiline is okay.
   DEPS='
   {:deps {
           clj-http/clj-http {:mvn/version "3.12.3"}
           cheshire/cheshire {:mvn/version "5.11.0"}
           org.clojure/tools.cli {:mvn/version "1.0.214"}
           net.clojars.wkok/openai-clojure {:mvn/version "0.15.0"}
           }}
   '

   #_You can put other options here
   OPTS='
   -J-Xms4m -J-Xmx256m
   '
#_Install Clojure if not present on the system (java is needed though)
if [[ ! -x .local/bin/clojure ]]; then
  [[ ! -d .local ]] && mkdir .local
  pushd .local
  curl -O https://download.clojure.org/install/posix-install-1.11.1.1273.sh
  chmod +x posix-install-1.11.1.1273.sh
  ./posix-install-1.11.1.1273.sh -p $PWD
  popd
fi

exec clojure $OPTS -Sdeps "$DEPS" -M "$0" "$@"
)

(require 'wkok.openai-clojure.api)
(require 'clj-http.client)
(require 'clojure.java.io)

(defn write-file
  "given content as a stream and a path, output the content to the given path"
  [content path]
  (let [file (clojure.java.io/file path)
        out (clojure.java.io/output-stream (.getPath file))]
    (try (clojure.java.io/copy content out)
         (finally (.close out)))))

(let [{:keys [data]}
      (wkok.openai-clojure.api/create-image
       {:model "dall-e-3"
        :size "1792x1024"
        :prompt "A picture of earth from the moon with a laptop displaying a search engine, the background should be transparent"}
       {:api-key "..."})]
  (-> (clj-http.client/get (:url (first data))
                           {:as :stream})
      :body
      (write-file "../resources/public/doodle.png")))
