name := "server"

version := "1.0"

lazy val `server` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "5.1.0.Final", // replace by your jpa implementation
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42"
)

PlayKeys.externalizeResources := false

libraryDependencies += filters

