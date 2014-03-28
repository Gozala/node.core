/**
 * @constructor
 * @extends events.EventEmitter
 */
var process = function() {
};

/**
 * @param name
 * @return {*}
 */
process.binding = function(name) {};

/**
 * @type {stream.ReadableStream}
 */
process.stdin;

/**
 * @type {stream.WritableStream}
 */
process.stdout;

/**
 * @type {stream.WritableStream}
 */
process.stderr;

/**
 * @type {Array.<string>}
 */
process.argv;

/**
 */
process.abort = function() {};

/**
 * @param {string} directory
 */
process.chdir = function(directory) {};

/**
 * @return {string}
 * @nosideeffects
 */
process.cwd = function() {};

/**
 * @type {Object.<string,string>}
 */
process.env;

/**
 * @param {number=} code
 */
process.exit = function(code) {};

/**
 * @return {number}
 * @nosideeffects
 */
process.getgid = function() {};

/**
 * @param {number} id
 */
process.setgid = function(id) {};

/**
 * @return {number}
 * @nosideeffects
 */
process.getuid = function() {};

/**
 * @param {number} id
 */
process.setuid = function(id) {};

/**
 * @type {!string}
 */
process.version;


/**
 * @param {number} pid
 * @param {string=} signal
 */
process.kill = function(pid, signal) {};

/**
 * @type {number}
 */
process.pid;

/**
 * @type {string}
 */
process.title;

/**
 * @type {string}
 */
process.arch;

/**
 * @type {string}
 */
process.platform;

/**
 * @return {Object.<string,number>}
 * @nosideeffects
 */
process.memoryUsage = function() {};

/**
 * @param {!function()} callback
 */
process.nextTick = function(callback) {};

/**
 * @param {number=} mask
 */
process.umask = function(mask) {};

/**
 * @return {number}
 * @nosideeffects
 */
process.uptime = function() {};

/**
 * @return {number}
 * @nosideeffects
 */
process.hrtime = function() {};
