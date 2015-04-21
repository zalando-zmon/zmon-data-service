Worker send its data to the zmon-data-service, which is itself responsible for:
 * storing it in Redis for frontend
 * storing it in KairosDB for charting
 * track size/rate by team
 * handle notifications (if we cannot do this in a distributed fashion (sms vs email))


input object:
  
{
    "account":,
    "team":,
    "results": [
        {
            "time": ""
            "check_id": 1234
            "check_result": ...,
            "run_time":,
            "exception": 0/1,
            "entity_id": "",
            "alerts" : {
                1 : { "state": 0/1, "captures": {}}, ...
            }
        }
    ]
}