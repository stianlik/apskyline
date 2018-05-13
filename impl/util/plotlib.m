function cmp = cstrcmp( a, b )

    % Force the strings to equal length
    x = char({a;b});

    % Subtract one from the other
    d = x(1,:) - x(2,:);

    % Remove zero entries
    d(~d) = [];
    if isempty(d)
        cmp = 0;
    else
        cmp = d(1);
    end

end

function printNames()
	global testdata;

	names = {};
	for i = 1:length(testdata)
		names(i) = testdata(i).name;
	end
	legend(names);
end

function r = getTestIndex(name)
	global testdata;

	% Name translation because we are great planners!
	from = { 'PPSkyline', 'DAPSkyline', 'APSSampleDynamicFair' };
	to = { 'APSEquiVolume', 'APSSampleDynamic', 'APSSampleDynamic+' };
	for i = 1:length(from)
		if strcmp(name,from{i})
			name = to{i};
			break;
		end
	end

	% And, now we're ready to do the actual work
	r = -1;
	for i = 1:length(testdata)
		if strcmp(testdata(i).name, name) == 1
			r = i;
			break;
		end
	end
	if r == -1
		r = length(testdata)+1;
		testdata(r).name = name;
		testdata(r).values = [];
		testdata(r).errors = [];
		testdata(r).skyline_size = [];
		testdata(r).partitioning = [];
		testdata(r).merge = [];
		testdata(r).local_skylines = [];
	end
end

function loadResult(file)
	global testdata

	load(file);
	i = getTestIndex(test.name);

	% Calculate statistics
	value = median(test.result);
	valueMax = max(test.result);
	valueMin = min(test.result);
	valueErr = max([valueMax-value, value-valueMin]);

	% Add result
	testdata(i).value(end+1) = test.value;
	testdata(i).values(end+1) = value;
	testdata(i).errors(end+1) = valueErr;

	% Init optional values
	testdata(i).skyline_size(end+1) = 0;
	testdata(i).partitioning(end+1) = 0;
	testdata(i).merge(end+1) = value;
	testdata(i).local_skylines(end+1) = 0;

	try
		testdata(i).skyline_size(end) = median(test.skyline_size);
		testdata(i).partitioning(end) = median(test.partitioning);
		testdata(i).merge(end) = median(test.merge);
		testdata(i).local_skylines(end) = median(test.local_skylines);
	catch
	end
end

function loadResults(files)
	for i = 1:length(files)
		if exist(files{i}, "file")
			loadResult(files{i});
		end
	end
	sortResults();
	styleResults();
	verifyResults();
	xtickResults();
end

function xtickResults()
	global testdata;
	for i = 1:length(testdata)
		testdata(i).xticks = { 'aaah!' };
		for j = 1:length(testdata(i).value)
			testdata(i).xticks(j) = num2str(testdata(i).value(j));
		end
	end
end

function R = translateTick(val, from, to)
	for i = 1:length(from)
		if from(i) == val
			R = to(i);
			return
		end
	end
	R = num2str(val);
end

function translateTicks(from, to) 
	global testdata;
	for i = 1:length(testdata)
		for j = 1:length(testdata(i).value)
			testdata(i).xticks(j) = translateTick(testdata(i).value(j), from, to);
		end
	end
end

function styleResults()
	global testdata;

	names = {'APSEquiVolume', 'APSSampleDynamic',\
	'PSkyline', 'ParallelBNL', 'ParallelBNL-2CPU', 'Java'};
	styles = [ 1 2 3 4 4 4 ];
	customs = max(styles)+1:100;

	k = max(styles) + 1;
	for i = 1:length(testdata)
		found = 0;
		for j = 1:length(names)
			if strcmp(names(j),testdata(i).name)
				testdata(i).style = styles(j);
				found = 1;
				break;
			end
		end
		if found == 0
			testdata(i).style = k;
			k = k + 1;
		end
	end
end


function sortResults()
	global testdata;
	% blob blob.. 
	for i = 1:length(testdata)
		for j = i:length(testdata)
			if cstrcmp(testdata(i).name, testdata(j).name) > 1
				tmp = testdata(i);
				testdata(i) = testdata(j);
				testdata(j) = tmp;
			end
		end
	end
end

function verifyResults()
	global testdata;

	% Avoid duplicate value
	%for i = 1:length(testdata)
	%	for j = 1:length(testdata(i).value)
	%		value = -1;
	%		for k = 1:length(testdata(i).value)
	%			if testdata(i).value(k) == value
	%				error(cstrcat("Duplicate value entry for test:"," '", testdata(i).name, "'"));
	%			end
	%			value = testdata(i).value(k);
	%		end
	%	end
	%end

	% All threads should have equal value array
	for i = 2:length(testdata)
		if length(testdata(1).value) != length(testdata(i).value)
			error('Tests do not have en equal amount of values')
		end

		for j = 1:length(testdata(1).value)
			if (testdata(1).value(j) != testdata(i).value(j))
				error('Tests do not have same order and value for values')
			end
		end
	end
end

function R = getColor(i)
	r = (mod(i,2) > 0) * 0.5;
	g = (mod(i,3) > 0) * 0.5;
	b = (mod(i,4) > 0) * 0.5;
	R = [ r g b ];
end

function ticksAndLabel(xlabels, ylabels, namex = 'Thread count', namey = 'Time (seconds)')
	% Configure ticks
	yscale = (ylim()(2) - ylim()(1))/25;
	ymin = ylim()(1);
	for i = 1:length(xlabels)
		x = i;
		text(x,ymin-yscale, xlabels(i), 'rotation', -90);
	end
	set(gca, 'xticklabel', '')

	% Label axes
	text(length(xlabels)/2-length(namex)*0.030,ymin-yscale*4,namex);
	ylabel(ylabels);
end

function plotResults(x = 'Thread count', y = 'Time (seconds)', type = 'normal', referenceIndex = 1, limit = 6)
	global testdata;
	hold on;

	limit = min(limit, length(testdata(1).values));
	styles = {"-s", "-^", "-o", "-x", "-", "-."};

	% Plot values
	if strcmp(type, 'diff')
		% Diff between two values
		assert(length(testdata) == 2)
		diff = testdata(2).values ./ testdata(1).values
		h = plot(diff, styles{5})
	elseif strcmp(type, 'distribution')
		yend = 0;
		placement = [];
		offset = -(length(testdata)-1)*0.5;
		width = 1/(length(testdata))-0.1/length(testdata);
		width = min(width,0.225);
		for i = 1:length(testdata)
			style = styles{testdata(i).style};
			placement = [0.63+offset*width+(i-1)*2.1*width];
			%h = bar(placement, testdata(i).values*1e3, width, "facecolor", [ 0.25*(i-1) 0.25*(i-1) 0.25*(i-1) ]);
			yend = max(max(testdata(i).values*1e3),yend);

			part = testdata(i).partitioning;
			if (sum(testdata(i).merge) + sum(testdata(i).local_skylines) + sum(testdata(i).partitioning)) == 0
				merge = testdata(i).values;
			else
				merge = testdata(i).merge;
			end
			h = bar(placement(1:limit), 1e3*(merge .+ part .+ testdata(i).local_skylines)(1:limit),width, "facecolor", [ 0.25 0.25 0.25 ]);
			h = bar(placement(1:limit), 1e3*(testdata(i).local_skylines .+ part)(1:limit), width, "facecolor", [ 0.75 0.75 0.75 ]);
			h = bar(placement, 1e3*part, width, "facecolor", [ 0.5 0.5 0.5 ]);
		end
		xlim([ 0 length(testdata(1).values)+1 ]);
		ylim([ 0 yend*1.3 ]);
		yscale = (ylim()(2) - ylim()(1))/25;
		ylabel(y);
		for i = 1:length(testdata)
			placement = [0.63+offset*width+(i-1)*2.1*width];
			%text(placement(1),ylim()(1)-yscale, num2str(i), 'rotation', -90);
			text(placement(1),ylim()(1)-yscale, testdata(i).name, 'rotation', -90);
		end
		%xlabel('Algorithm')
		legend({'Global skyline' 'Local skyline' 'Partitioning'});
		set(gca, 'xticklabel', '')
	elseif strcmp(type, 'dimensionality')
		yend = 0;
		for i = 1:length(testdata)
			style = styles{testdata(i).style};
			width = 1/(length(testdata))-0.1/length(testdata);
			offset = -(length(testdata)-1)*0.5;
			placement = [1+offset*width+(i-1)*width:1:length(testdata(i).values)+(i-2)*width];
			h = bar(placement, testdata(i).values, width, "facecolor", [ 0.24*(i-1) 0.24*(i-1) 0.24*(i-1) ]);
			yend = max(max(testdata(i).values),yend);
		end
		xlim([ 0 length(testdata(1).values)+1 ]);
		ylim([ 0 yend*1.35 ]);
		printNames();
		ticksAndLabel(testdata(1).xticks, y, x);
	elseif strcmp(type, 'segmented')
		yend = 0;
		for i = 1:length(testdata)
			yend = max(max(testdata(i).values(1:limit)),yend);
			%xlim([ 0 length(testdata(i).values)+1 ]);
			width = 1/(length(testdata))-0.1/length(testdata);
			offset = -(length(testdata)-1)*0.5;
			placement = [1+offset*width+(i-1)*width:1:length(testdata(i).values)+1];
			part = testdata(i).partitioning;
			if (sum(testdata(i).merge) + sum(testdata(i).local_skylines) + sum(testdata(i).partitioning)) == 0
				merge = testdata(i).values;
			else
				merge = testdata(i).merge;
			end
			%h = bar(placement, testdata(i).values, width, "facecolor", [ 0 0 1 ]);
			h = bar(placement(1:limit), (merge .+ part .+ testdata(i).local_skylines)(1:limit),width, "facecolor", [ 0.25 0.25 0.25 ]);
			h = bar(placement(1:limit), (testdata(i).local_skylines .+ part)(1:limit), width, "facecolor", [ 0.75 0.75 0.75 ]);
			h = bar(placement(1:limit), part(1:limit), width, "facecolor", [ 0.5 0.5 0.5 ]);
		end
		ylim([ 0 yend*1.3 ]);
		ticksAndLabel(testdata(1).xticks(1:limit), y, x);
		legend({'Global skyline' 'Local skyline' 'Partitioning'});
	else
		% Normal plot
		xlim([ 1 length(testdata(1).values) ]);
		biggestyval = 0;
		for i = 1:length(testdata)
			style = styles{testdata(i).style};
			if strcmp(type, 'errorbar')
				h = errorbar(testdata(i).values, testdata(i).errors);
			elseif strcmp(type, 'speedup')
				speedup = testdata(i).values(1) ./ testdata(i).values;
				h = plot(speedup, style);
				biggestyval = max(biggestyval, max(speedup));
				ylim([ 0 biggestyval*1.2 ]);
			elseif strcmp(type, 'perstuple')
				values = testdata(i).values ./ testdata(i).skyline_size;
				h = plot(values .*1e6, style);
			elseif strcmp(type, 'speedup_minus_partitioning')
				values = testdata(i).values .- testdata(i).partitioning;
				speedup = values(1) ./ values;
				h = plot(speedup, style);
			elseif strcmp(type, 'skyline_size')
				h = plot(1:length(testdata(i).skyline_size), testdata(i).skyline_size, style);
			elseif strcmp(type, 'normal')
				h = plot(1:length(testdata(i).values), testdata(i).values, style);
			elseif strcmp(type, 'time_minus_partitioning')
				h = plot(1:length(testdata(i).values), testdata(i).values .- testdata(i).partitioning, style);
			elseif strcmp(type, 'time_partitioning') == 1
				h = plot(1:length(testdata(i).values), testdata(i).partitioning, style);
			elseif strcmp(type, 'time_merge') == 1
				h = plot(1:length(testdata(i).values), testdata(i).merge, style);
			else
				error('Invalid graph type')
			end

			%ylim([ 0 ylim()(2) ]);

			%set(h(1), 'color', getColor(i));
			set(h(1), 'color', [0 0 0]);
		end

		% Configure ticks
		set(gca, 'xtick', 1:length(testdata(1).value));
		ticksAndLabel(testdata(1).xticks, y, x);

		% Name axes
		if strcmp(type, 'apskyline')
			legend({'Global skyline (merge)' 'Local skylines' 'Partitioning'});
		else
			names = {};
			for i = 1:length(testdata)
				names(i) = testdata(i).name;
			end
			legend(names);
		end

	end

	hold off;
end

function printPlot(filename)
	print(filename, '-dpdf', "-F:18");
end

