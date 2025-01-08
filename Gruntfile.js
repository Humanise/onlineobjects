module.exports = function(grunt) {

  const sass = require('sass');

  // Project configuration.
  config = {
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
      }
    },
    sass: {
      core: {
        options: {
          implementation: sass,
          sourceMap: false,
          api: 'modern'
        },
        files: [{
          expand: true,
          cwd: 'src/main/webapp/WEB-INF/core/web/scss/',
          src: ['*.scss'],
          dest: 'src/main/webapp/WEB-INF/core/web/css',
          ext: '.css'
        }]
      }
    }
  };

  ['account','words','knowledge','photos','people','front','developer'].forEach((app) => {
    config.watch[app] = {
      files: ['src/main/webapp/WEB-INF/apps/'+app+'/web/scss/**/*.scss','src/main/webapp/apps/'+app+'/scss/**/*.scss'],
      tasks: ['sass:'+app],
      options: {
        spawn: false,
      }
    }
    config.sass[app] = {
      options: {
        implementation: sass,
        sourceMap: false,
        api: 'modern'
      },
      files: [{
        expand: true,
        cwd: 'src/main/webapp/WEB-INF/apps/' + app + '/web/scss/',
        src: ['*.scss'],
        dest: 'src/main/webapp/WEB-INF/apps/' + app + '/web/css',
        ext: '.css'
      },{
        expand: true,
        cwd: 'src/main/webapp/apps/' + app + '/scss/',
        src: ['*.scss'],
        dest: 'src/main/webapp/apps/' + app + '/css',
        ext: '.css'
      }]
    }
  })
  grunt.initConfig(config);

  // Load plugins.
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-sass-modern');

  // Default task(s).
  grunt.registerTask('default', 'Watch', ['sass','watch']);

};