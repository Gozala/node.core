// node's process object has bunch of mutable fields like
// `process.title`, `process.maxTickDepth`, etc.. and a
// setter methods like `process.setgid`, `process.umask`.
// They all cause side effects that aren't observable without
// in any ways without polling. This hack is a workaround to
// fix this: https://github.com/joyent/node/issues/7373
// It replaces `process` with a proxy to it that emit's events
// to make those side effects observable.
process = (function(process) {
  "use strict";

  // Creates a method descriptor that dispatches to
  // a method on `target` that has a given `name`.
  function makeProxyMethod(target, name) {
    return {
      enumerable: true,
      value: function() {
        return target[name].apply(target, arguments);
      }
    };
  }

  // Creates a getter / setter destriptor that reads / writes
  // field with a given `name` on a given `target`.
  function makeProxyField(target, name) {
    return {
      enumerable: true,
      get: function() {
        return target[name]
      },
      set: function(value) {
        return target[name] = value
      }
    };
  }

  // Just like `makeProxyField` with a difference that event
  // with a given `name` is emitted on `target` when field
  // is updated.
  function makeObservableField(target, name) {
    return {
      enumerable: true,
      get: function() {
        return target[name];
      },
      set: function(value) {
        target.emit(name, target[name] = value);
      }
    }
  }

  // Just like `makeProxyMethod` with a differenec that event
  // with a given `name` is emitted after arguments are dispatched
  // to a target. Optionally takes `setter` method name and
  // `getter` method names, if not provided assumes they are
  // `"set" + name` / `"get" + name`.
  function makeObservableMethod(target, name, setter, getter) {
    var set = target[setter || "set" + name];
    var get = target[getter || "get" + name];
    return {
      enumerable: true,
      value: function() {
        var result = set.apply(target, arguments);
        target.emit(name, get.call(target));
        return result;
      }
    }
  }

  var names = Object.getOwnPropertyNames(process);
  var descriptor = names.reduce(function(descriptor, name) {
    var isMethod = typeof(process[name]) === "function";
    var makeProxy = isMethod ? makeProxyMethod : makeProxyField;
    descriptor[name] = makeProxy(process, name);
    return descriptor;
  }, {});
  descriptor.title = makeObservableField(process, "title");
  descriptor.maxTickDepth = makeObservableField(process, "maxTickDepth");
  descriptor.setgid = makeObservableMethod(process, "gid");
  descriptor.setuid = makeObservableMethod(process, "uid");
  descriptor.setgroups = makeObservableMethod(process, "groups");
  descriptor.initgroups = makeObservableMethod(process, "groups",
                                               "initgroups",
                                               "getgroups");
  descriptor.chdir = makeObservableMethod(process, "cwd",
                                          "chdir", "cwd");
  // This property is special & is used by `setImmediate` there for
  // it need's to be proxied.
  descriptor._immediateCallback = makeProxyField(process, "_immediateCallback");
  // This method is kind of awkward as it serves both as getter and
  // setter. Since we only care about setter use case we override it
  // emit events only on set.
  descriptor.umask = {
    enumerable: true,
    value: function() {
      var result = process.umask.apply(process, arguments);
      if (arguments.length)
        process.emit("umask", process.umask())

      return result;
    }
  };

  return module.exports = Object.create(process, descriptor);
})(process);
