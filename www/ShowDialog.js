var exec = require('cordova/exec');

exports.showDialog = function (success, error) {
    exec(success, error, 'ShowDialog', 'showDialog', []);
};
