/**
BEGIN_NODE_INCLUDE
var events = require('events');
END_NODE_INCLUDE
 */

/**
 * @type {Object.<string,*>}
 */
var events = {};

/**
 * @constructor
 */
events.EventEmitter = function() {};

/**
 * @param {string} event
 * @param {function(...)} listener
 * @return {events.EventEmitter}
 */
events.EventEmitter.prototype.addListener = function(event, listener) {};

/**
 * @param {string} event
 * @param {function(...)} listener
 * @return {events.EventEmitter}
 */
events.EventEmitter.prototype.on = function(event, listener) {};

/**
 * @param {string} event
 * @param {function(...)} listener
 * @return {events.EventEmitter}
 */
events.EventEmitter.prototype.once = function(event, listener) {};

/**
 * @param {string} event
 * @param {function(...)} listener
 * @return {events.EventEmitter}
 */
events.EventEmitter.prototype.removeListener = function(event, listener) {};

/**
 * @param {string=} event
 * @return {events.EventEmitter}
 */
events.EventEmitter.prototype.removeAllListeners = function(event) {};

/**
 * @param {number} n
 */
events.EventEmitter.prototype.setMaxListeners = function(n) {};

/**
 * @param {string} event
 * @return {Array.<function(...)>}
 */
events.EventEmitter.prototype.listeners = function(event) {};

/**
 * @param {string} event
 * @param {...*} var_args
 * @return {boolean}
 */
events.EventEmitter.prototype.emit = function(event, var_args) {};

// Undocumented

/**
 * @type {boolean}
 */
events.usingDomains;

/**
 * @param {events.EventEmitter} emitter
 * @param {string} type
 */
events.EventEmitter.listenerCount = function(emitter, type) {};
