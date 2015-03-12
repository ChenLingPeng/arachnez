name := "Arachnez"

version := "1.0"

scalaVersion := "2.11.2"

javacOptions ++= Seq("-encoding", "UTF-8")



libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-jdbc" % "2.3.3",
  "com.typesafe.play" %% "anorm" % "2.3.3",
  "mysql" % "mysql-connector-java" % "5.1.31"
)

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.5" withSources()

libraryDependencies += "log4j" % "log4j" % "1.2.16"

libraryDependencies += "org.jsoup" % "jsoup" % "1.7.2"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.1"

libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.0"

libraryDependencies += "commons-dbutils" % "commons-dbutils" % "1.5"

libraryDependencies += "com.mchange" % "c3p0" % "0.9.2.1"

libraryDependencies += "org.json" % "json" % "20140107"


