'use strict';

module.exports = function (grunt) {
  require('load-grunt-tasks')(grunt);

  grunt.initConfig({
    wiredep: {
      app: {
        src: ['./index.html']
      }
    },
    wiredepCopy: {
      target: {
        options: {
          src: 'app',
          dest: 'dist/app/',
          wiredep: {}
        }
      },
    },

    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '.',
          src: ['app/**', 'img/**', 'app.css', 'app.js', 'favicon.ico', '*.html',
            '!app/bower_components/**', '!node_modules'],
          dest: 'dist'
        }]
      }
    },

    clean: {
      build: ['dist']
    }
  });

  // copy distribution to ./dist/ dir with bower dependencies
  grunt.registerTask('build', [
    'wiredep',
    'copy',
    'wiredepCopy'
  ]);

  grunt.registerTask('default', [
    'build'
  ]);
};