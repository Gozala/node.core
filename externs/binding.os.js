/**
 BEGIN_NODE_INCLUDE
 var os = process.binding('os');
 END_NODE_INCLUDE
 */

var os = {};

/**
 * @return {string}
 * @nosideeffects
 */
os.tmdDir = function() {};

/**
 * @return {string}
 * @nosideeffects
 */
os.hostname = function() {};

/**
 * @return {string}
 * @nosideeffects
 */
os.type = function() {};

/**
 * @return {string}
 * @nosideeffects
 */
os.platform = function() {};

/**
 * @return {string}
 * @nosideeffects
 */
os.arch = function() {};

/**
 * @return {string}
 * @nosideeffects
 */
os.release = function() {};

/**
 * @return {number}
 * @nosideeffects
 */
os.uptime = function() {};

/**
 * @return {Array.<number>}
 * @nosideeffects
 */
os.loadavg = function() {};

/**
 * @return {number}
 * @nosideeffects
 */
os.totalmem = function() {};

/**
 * @return {number}
 * @nosideeffects
 */
os.freemem = function() {};

/**
 * @typedef {{model: string, speed: number, times: {user: number, nice: number, sys: number, idle: number, irg: number}}}
 */
var osCpusInfo;

/**
 * @return {Array.<osCpusInfo>}
 * @nosideeffects
 */
os.cpus = function() {};

/**
 * @typedef {{address: string, family: string, internal: boolean}}
 */
var osNetworkInterfacesInfo;

/**
 * @return {Object.<string,osNetworkInterfacesInfo>}
 * @nosideeffects
 */
os.networkInterfaces = function() {};

/**
 * @type {string}
 */
os.EOL;
