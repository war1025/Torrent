
classpath :
	export CLASSPATH=./:/usr/share/java/dbus.jar

compile :
	javac ./tcl/TorrentClient.java

clean :
	git clean -f
