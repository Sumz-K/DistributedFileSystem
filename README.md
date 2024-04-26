Built a distributed file system in Java that is inspired by the Hadoop Distributed File System(HDFS). The architecture resembles that of HDFS with datanode containers storing the actual data and a namenode container that manages all metadata and facilitates all client operations.

Our system has 4 datanode containers, a namenode container and a datanode manager container along with a mongoDB database with 2 collections. Our system supports file writes(uploads), file reads(downloads) and file deletion.


Additionally, we build a client desktop application with Java Swing that allows you to perform all these operations and the backend was purely build in Java with no usage of Swing.

