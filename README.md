# IR server for teaching purposes.

Student projects in Information Retrieval courses are often limited since building an inverted index of a big text collection takes time, space and is not trivial. Existing APIs also include complex retrieval models which students should implement by themselves rather than reusing. This project wraps a RESTful web service around a lucene index. Allowing students to query useful statistics from the index and build their own retrieval models, without having unlimited access to the index. The project is implemented in Java and you will need a Java Runtime Environment (1.8)to run it.

See ./manual for documentation.
