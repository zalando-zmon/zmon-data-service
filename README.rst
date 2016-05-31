=================
ZMON Data Service
=================

Worker sends its data to the zmon-data-service, which is itself responsible for:

* storing it in Redis for frontend
* storing it in KairosDB for charting
* track size/rate by team
* handle notifications (if we cannot do this in a distributed fashion (sms vs email))


Input object:

.. code-block:: json

    {
        "account": "",
        "team": "",
        "results": [
            {
                "time": ...,
                "check_id": 1234,
                "check_result": ...,
                "run_time": ...,
                "exception": 0/1,
                "entity_id": "",
                "alerts" : {
                    1 : { "state": 0/1, "captures": {}}, ...
                }
            }
        ]
    }

Building
========

.. code-block:: bash

    $ ./mvnw clean package
    $ sudo pip3 install scm-source
    $ scm-source -f target/scm-source.json
    $ docker build -t zmon-data-service .

Running
=======

.. code-block:: bash

    $ export TOKENINFO_URL=...
    $ java -jar target/zmon-data-service-1.0-SNAPSHOT.jar
