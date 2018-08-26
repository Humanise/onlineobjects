module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    jshint: {
        all: ['js/*.js']
    },
    watch: {
      core: {
        files: ['src/main/webapp/WEB-INF/core/web/scss/**/*.scss'],
        tasks: ['sass'],
        options: {
          spawn: false,
        }
      },
      reader: {
        files: ['src/main/webapp/WEB-INF/apps/reader/web/scss/**/*.scss'],
        tasks: ['sass:reader'],
        options: {
          spawn: false,
        }
      },
      words: {
        files: ['src/main/webapp/WEB-INF/apps/words/web/scss/**/*.scss'],
        tasks: ['sass:words'],
        options: {
          spawn: false,
        }
      },
      account: {
        files: ['src/main/webapp/WEB-INF/apps/account/web/scss/**/*.scss'],
        tasks: ['sass:account'],
        options: {
          spawn: false,
        }
      }
    },
    sass: {
      core: {
        options : {sourcemap:'none'},
        files: [{
          expand: true,
          cwd: 'src/main/webapp/WEB-INF/core/web/scss/',
          src: ['*.scss'],
          dest: 'src/main/webapp/WEB-INF/core/web/css',
          ext: '.css'
        }]
      },
      reader: {
        options : {sourcemap:'none'},
        files: [{
          expand: true,
          cwd: 'src/main/webapp/WEB-INF/apps/reader/web/scss/',
          src: ['*.scss'],
          dest: 'src/main/webapp/WEB-INF/apps/reader/web/css',
          ext: '.css'
        }]
      },
      words: {
        options : {sourcemap:'none'},
        files: [{
          expand: true,
          cwd: 'src/main/webapp/WEB-INF/apps/words/web/scss/',
          src: ['*.scss'],
          dest: 'src/main/webapp/WEB-INF/apps/words/web/css',
          ext: '.css'
        }]
      },
      account: {
        options : {sourcemap:'none'},
        files: [{
          expand: true,
          cwd: 'src/main/webapp/WEB-INF/apps/account/web/scss/',
          src: ['*.scss'],
          dest: 'src/main/webapp/WEB-INF/apps/account/web/css',
          ext: '.css'
        }]
      }
    }
  });

  // Load plugins.
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-sass');

  // Default task(s).
  grunt.registerTask('default', 'Watch', ['sass','watch']);

};