#!/usr/bin/env ruby

require 'pathname'
require 'json'

class Fetcher

  def initialize(args)
    @base = Pathname.new(__FILE__).parent.parent
    @urls = [
      'https://www.nngroup.com/articles/ten-usability-heuristics/',
      'https://www.bolius.dk/10-gode-raad-naar-du-skal-kvitte-oliefyret-901/',
      'https://www.quantamagazine.org/famous-experiment-dooms-pilot-wave-alternative-to-quantum-weirdness-20181011/',
      'https://asktog.com/atc/principles-of-interaction-design/',
      'https://www.dr.dk/nyheder/penge/klodser-i-fremgang-det-koerer-lego',
      'https://politiken.dk/debat/art5592320/Krisen-har-%C3%B8get-afstanden-mellem-eliten-og-alle-de-andre',
      'https://alistapart.com/article/long-term-design-rewriting-the-design-sales-pitch',
      'http://arnosoftwaredev.blogspot.com/2011/01/hibernate-performance-tips.html',
      'https://www.information.dk/debat/2018/11/skaber-ulighed-sundhed-folk-betale-tandlaegeregningen',
      'https://signalvnoise.com/posts/3144-what-clarity-is-all-about',
      'https://www.worldwildlife.org/press-releases/wwf-report-reveals-staggering-extent-of-human-impact-on-planet'
    ]
    if args[0]
      @urls = [args[0]]
    end
  end

  def run
    @urls.each do |url|
      pretty = url.gsub(/https?:\/\/(www\.)?/i, "").gsub(/\W+/i, "-").gsub(/\-$/i, "")
      pretty_short = url.gsub(/https?:\/\//i, "").split('/').last.gsub(/\W+/i, "-")[0..20]
      folder = @base.join('src/test/resources/extraction').join("#{pretty}");
      folder.mkdir unless folder.exist?
      archive = folder.join("#{pretty_short}.webarchive")
      original = folder.join("#{pretty_short}.original.html")
      info = folder.join("#{pretty_short}.json")
      text = folder.join("#{pretty_short}.ideal.txt")
      system "webarchiver -url #{url} -output #{File.expand_path(archive)}" unless archive.exist?
      system "curl '#{url}' --output #{File.expand_path(original)}" unless original.exist?
      info.write JSON.pretty_generate({url: url}) unless info.exist?
      text.write 'TODO' unless text.exist?
    end
  end
end

if __FILE__ == $0
  x = Fetcher.new(ARGV)
  x.run # or go, or whatever
end