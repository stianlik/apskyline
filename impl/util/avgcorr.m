function r = avgcorr(D)
	dim = size(D)(2);
	r = mean(sum(corr(D) - eye(dim,dim)) ./ (dim-1));
end
