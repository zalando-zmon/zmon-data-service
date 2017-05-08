-- checkAlerts.lua
local members = redis.call('smembers', KEYS[1]);
if table.getn(members) == 0 then
  redis.call('srem','zmon:alert-acks', ARGV[1]);
  return redis.call('srem','zmon:alerts', ARGV[1]);
else
  return redis.call('sadd','zmon:alerts', ARGV[1]);
end
