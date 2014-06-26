require 'fileutils'

# various latex commands
begin_document = /\\begin\{document\}/
chapter = /\\chapter\*?\{([\w \.,:;\-]+)\}/
section = /\\section\*?\{([\w \.,:;\-]+)\}/
subsection = /\\subsection\*?\{([\w \.,:;\-]+)\}/
subsubsection = /\\subsubsection\*?\{([\w \.,:;\-]+)\}/
autoref = /\\autoref\{([\w \.,:;\-]+)\}/
textit = /\\textit\{([\w \.,:;\-]+)\}/
autocite = /~\\autocite\{([\w \.,:;\-]+)\}/
nom = /\\nom\{([\w \.,:;\-]+)\}\{([\w \.,:;\-]+)\}/
footnote_url = /\\footnote\{\\url\{[\w \.,:;\-\/\?=]+\}\}/

# minted code listings
inputminted = /\\inputminted\[/
minted_1 = /\\[a-z]+\*?\{\\[a-z]+\}/
minted_2 = /\t+[a-z]+,/
minted_3 = /\t+[a-z]+=[\w\-\.,\\]+/
minted_4 = /\t+\]\{[a-z]+\}\{[\w\/\-\.]+\}/

# tables
table_1 = /\{(\w+(\[[\w]+\])?)+\|\[[\w\.]+\](\w+(\[[\w]*\])?)+\}/
table_2 = /\s*\&.*/
table_3 = /\\multirow.*/

# every line starting with a tab
tab = /^\t.*/

# \xyz[]{}
generic = /~?\\[a-z]+\*?(\[[\w \.,:;\-!=\/]*\]|\{[\w \.,:;\-!=\/]*\})*/

# lines containing only whitespace
whitespace = /^\s+$/

skip = true
last_line = ''
output = File.new('thesis.txt', 'w');

File.open('tmp.tex', 'r') do |f|
	f.each_line do |line|
		if skip && line =~ /^\\begin\{document\}/
			skip = false
		end
		if !skip
			output_line = line
			case line
			when begin_document
			else
				line.gsub!(chapter, '\1')
				line.gsub!(section, '\1')
				line.gsub!(subsection, '\1')
				line.gsub!(subsubsection, '\1')
				line.gsub!(textit, '\1')
				line.gsub!(autoref, 'Autoref')
				line.gsub!(nom, '\1 (\2)')
				line.gsub!(footnote_url, ' (URL)')
				line.gsub!(inputminted, '')
				line.gsub!(minted_1, '')
				line.gsub!(minted_2, '')
				line.gsub!(minted_3, '')
				line.gsub!(minted_4, '')
				line.gsub!(table_1, '')
				line.gsub!(table_2, '')
				line.gsub!(table_3, '')
				line.gsub!(generic, '')
				line.gsub!(tab, '')
				line.gsub!(whitespace, '')

				if line == '' && last_line == ''
					# do nothing
				else
					last_line = line
					output.puts line
				end
			end
		end
	end
end

output.close
