// Workaround for
// https://github.com/joyent/node/issues/7373
process = (function(process) {
  "use strict";
  function makeObservableField(name) {
    return {
      get: function() {
        return process[name];
      },
      set: function(value) {
        process.emit(name, process[name] = value);
      }
    }
  }

  function makeObservableSetter(name, setName, getName) {
    setName = setName || "set" + name;
    getName = getName || "get" + name;
    return {
      value: function() {
        var result = process[setName].apply(process, arguments);
        process.emit(name, process[getName]())
        return result;
      }
    }
  }

  return module.exports = Object.create(process, {
    title: makeObservableField("title"),
    maxTickDepth: makeObservableField("maxTickDepth"),
    setgid: makeObservableSetter("gid"),
    setuid: makeObservableSetter("uid"),
    setgroups: makeObservableSetter("groups"),
    initgroups: makeObservableSetter("groups", "initgroups", "getgroups"),
    chdir: makeObservableSetter("cwd", "chdir", "cwd"),
    umask: {
      value: function() {
        var result = process.umask.apply(process, arguments);
        if (arguments.length)
          process.emit("umask", process.umask())

        return result;
      }
    }
  });
})(process);
