(ns find-aot-deps.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set])
  (:import java.util.jar.JarFile
           (java.nio.file Files Path Paths FileVisitOption)))

(defn jar-files
  "Given the path to a .jar file on disk, return a seq of files contained in the zip"
  [path]
  (->> path
      (JarFile.)
      (.entries)
      (enumeration-seq)
      (map str)))

(defn jar-file? [path]
  (and (-> path io/file .exists)
       (-> path io/file .isFile)
       (boolean (re-find #".+\.jar$" path))))

(defn clj-file?
  "True if the path is a .clj file"
  [path]
  (re-find #"\.clj$" path))

(defn classfile? [path]
  (re-find #"\.class$" path))

(defn classfile-class
  "Given a path to a .class file, return the clj/java class it belongs to"
  [path]
  (-> path
      (#(re-find #"^([^$]*).*\.class$" %))
      second
      (str/replace "/" ".")
      (str/replace #"__init$" "")))

(defn clj-namespace [path]
  (-> (re-find #"^(.*)\.clj" path)
      second
      (str/replace "/" ".")))

(defn aot-clj?
  "True if the path represnts an AOT'd clojure namespace."
  [path]
  (re-find #"__init.class$" path))

(defn aot-clj-dep?
  "Returns a seq containing any AOT'd clojure namespaces in the jar that don't
  correspond to any .clj file in the jar"
  [jar-path]
  (let [files (jar-files jar-path)
        clj (->> files
                 (filter clj-file?)
                 (map clj-namespace)
                 (set))
        aot-clj (->> files
                     (filter aot-clj?)
                     (map classfile-class)
                     (set))]
    (seq (set/difference aot-clj clj))))

(defn m2-jars []
  "Scans your ~/.m2/repository, returns a seq of paths to jars"
  (->
   (Paths/get (System/getenv "HOME") (into-array String [".m2" "repository"]))
   (Files/walk (into-array FileVisitOption []))
   (.iterator)
   (iterator-seq)
   (->>
    (map str)
    (filter jar-file?))))

(defn offending-jars []
  (doseq [j (m2-jars)
          :let [aot (aot-clj-dep? j)]
          :when (seq aot)]
    (println j "contains" aot)))
