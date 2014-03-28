/**
 BEGIN_NODE_INCLUDE
 var net = require('net');
 END_NODE_INCLUDE
 */

/**
 * @type {Object.<string,*>}
 */
var net = {};

/**
 * @typedef {{allowHalfOpen: ?boolean}}
 */
net.CreateOptions;

/**
 * @param {(net.CreateOptions|function(...))=} options
 * @param {function(...)=} connectionListener
 * @return {net.Server}
 */
net.createServer = function(options, connectionListener) {};

/**
 * @typedef {{port: ?number, host: ?string, localAddress: ?string, path: ?string, allowHalfOpen: ?boolean}}
 */
net.ConnectOptions;

/**
 * @param {net.ConnectOptions|number|string} arg1
 * @param {(function(...)|string)=} arg2
 * @param {function(...)=} arg3
 */
net.connect = function(arg1, arg2, arg3) {};

/**
 * @constructor
 * @extends events.EventEmitter
 */
net.Server = function() {};

/**
 *
 * @param {number|*} port
 * @param {(string|number|function(...))=} host
 * @param {(number|function(...))=} backlog
 * @param {function(...)=} callback
 */
net.Server.prototype.listen = function(port, host, backlog, callback) {};

/**
 * @param {function(...)=} callback
 */
net.Server.prototype.close = function(callback) {};

/**
 * @return {{port: number, family: string, address: string}}
 */
net.Server.prototype.address = function() {};

/**
 * @type {number}
 */
net.Server.prototype.maxConnectinos;

/**
 * @type {number}
 */
net.Server.prototype.connections;

/**
 * @constructor
 * @param {{fd: ?*, type: ?string, allowHalfOpen: ?boolean}=} options
 * @extends events.EventEmitter
 */
net.Socket = function(options) {};

/**
 * @param {number|string|function(...)} port
 * @param {(string|function(...))=} host
 * @param {function(...)=} connectListener
 */
net.Socket.prototype.connect = function(port, host, connectListener) {};

/**
 * @type {number}
 */
net.Socket.prototype.bufferSize;

/**
 * @param {?string=} encoding
 */
net.Socket.prototype.setEncoding = function(encoding) {};

/**
 * @param {string|buffer.Buffer} data
 * @param {(string|function(...))=}encoding
 * @param {function(...)=} callback
 */
net.Socket.prototype.write = function(data, encoding, callback) {};

/**
 * @param {(string|buffer.Buffer)=}data
 * @param {string=} encoding
 */
net.Socket.prototype.end = function(data, encoding) {};

/**
 */
net.Socket.prototype.destroy = function() {};

/**
 */
net.Socket.prototype.pause = function() {};

/**
 */
net.Socket.prototype.resume = function() {};

/**
 * @param {number} timeout
 * @param {function(...)=} callback
 */
net.Socket.prototype.setTimeout = function(timeout, callback) {};

/**
 * @param {boolean=} noDelay
 */
net.Socket.prototype.setNoDelay = function(noDelay) {};

/**
 * @param {(boolean|number)=} enable
 * @param {number=} initialDelay
 */
net.Socket.prototype.setKeepAlive = function(enable, initialDelay) {};

/**
 * @return {string}
 */
net.Socket.prototype.address = function() {};

/**
 * @type {?string}
 */
net.Socket.prototype.remoteAddress;

/**
 * @type {?number}
 */
net.Socket.prototype.remotePort;

/**
 * @type {number}
 */
net.Socket.prototype.bytesRead;

/**
 * @type {number}
 */
net.Socket.prototype.bytesWritten;

/**
 * @param {*} input
 * @return {number}
 */
net.isIP = function(input) {};

/**
 * @param {*} input
 * @return {boolean}
 */
net.isIPv4 = function(input) {};

/**
 * @param {*} input
 * @return {boolean}
 */
net.isIPv6 = function(input) {};
