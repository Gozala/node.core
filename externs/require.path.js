/**
 BEGIN_NODE_INCLUDE
 var path = require('path');
 END_NODE_INCLUDE
 */

/**
 * @type {Object.<string,*>}
 */
var path = {};

/**
 * @type {string}
 */
path.delimiter;

/**
 * @type {string}
 */
path.sep;

/**
 * @param {string} path
 * @return {string}
 * @nosideeffects
 */
path.normalize = function(path) {};

/**
 * @param {...string} paths
 * @return {string}
 * @nosideeffects
 */
path.join = function(paths) {};

/**
 * @param {string} from
 * @param {string=} to
 * @return {string}
 * @nosideeffects
 */
path.resolve = function(from, to) {};

/**
 * @param {string} from
 * @param {string} to
 * @return {string}
 * @nosideeffects
 */
path.relative = function(from, to) {};

/**
 * @param {string} path
 * @return {string}
 * @nosideeffects
 */
path.dirname = function(path) {};

/**
 * @param {string} path
 * @param {string=} extension
 * @return {string}
 * @nosideeffects
 */
path.basename = function(path, extension) {};

/**
 * @param {string} path
 * @return {string}
 * @nosideeffects
 */
path.extname = function(path) {};

