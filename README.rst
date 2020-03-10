ZMON source code on GitHub is no longer in active development. Zalando will no longer actively review issues or merge pull-requests.

ZMON is still being used at Zalando and serves us well for many purposes. We are now deeper into our observability journey and understand better that we need other telemetry sources and tools to elevate our understanding of the systems we operate. We support the `OpenTelemetry <https://opentelemetry.io>`_ initiative and recommended others starting their journey to begin there.

If members of the community are interested in continuing developing ZMON, consider forking it. Please review the licence before you do.

=================
ZMON Data Service
=================

.. image:: https://img.shields.io/badge/OpenTracing-enabled-blue.svg
    :target: http://opentracing.io
    :alt: OpenTracing enabled

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
    $ docker build -t zmon-data-service .

Running
=======

.. code-block:: bash

    $ export TOKENINFO_URL=...
    $ java -jar target/zmon-data-service-1.0-SNAPSHOT.jar
