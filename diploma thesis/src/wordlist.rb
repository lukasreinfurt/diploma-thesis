require 'fileutils'

wordlist = Hash.new
output   = File.new('wordlist.txt', 'w')

File.open('thesis.txt', 'r') do |file|
	file.each_line do |line|
		words = line.split(/\W+/)
		words.each do |word|
			if wordlist.key? word
				wordlist[word] += 1
			else
				wordlist[word] = 1
			end
		end
	end
end

wordlist.sort.map do |key, value|
	output.puts "#{key} (#{value})"
end

output.close
