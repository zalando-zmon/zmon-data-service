-- count.lua
local count = redis.call('SCARD', KEYS[1]);
return count;
