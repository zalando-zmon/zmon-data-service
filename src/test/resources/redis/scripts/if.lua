-- ifThen.lua
local count = redis.call('SCARD', KEYS[1]);
if count == 0 then
  local r = redis.call('SREM',KEYS[1],ARGV[1]);
  return r;
end
