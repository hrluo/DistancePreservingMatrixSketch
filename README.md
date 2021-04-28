

# DistancePreservingMatrixSketch

**Content**
This is the code repository for the research publication "A Distance-preserving Matrix Sketch" by [Leland Wilkinson](https://www.cs.uic.edu/~wilkinson/) and [Hengrui Luo](https://hrluo.github.io/). 
The manuscript of this paper can be accessed at https://arxiv.org/abs/2009.03979. 

 - In [Python folder](https://github.com/hrluo/DistancePreservingMatrixSketch/tree/master/Python) and [R folder](https://github.com/hrluo/DistancePreservingMatrixSketch/tree/master/R), we provided a set of illustrative code that serves as a proof of concept and exhibits the sketching algorithm with extensive comments and remarks.
 - In [Java folder](https://github.com/hrluo/DistancePreservingMatrixSketch/tree/master/Java), we provided a set of robust code that can be executed for large datasets with additional features like _Johnson-Lindenstrauss algorithm_.

**Abstract**
Visualizing very large matrices involves many formidable problems. Various popular solutions to these problems involve sampling, clustering, projection, or feature selection to reduce the size and complexity of the original task. An important aspect of these methods is how to preserve relative distances between points in the higher-dimensional space after reducing rows and columns to fit in a lower dimensional space. This aspect is important because conclusions based on faulty visual reasoning can be harmful. Judging dissimilar points as similar or similar points as dissimilar on the basis of a visualization can lead to false conclusions. To ameliorate this bias and to make visualizations of very large datasets feasible, we introduce a new algorithm that selects a subset of rows and columns of a rectangular matrix. This selection is designed to preserve relative distances as closely as possible. We compare our matrix sketch to more traditional alternatives on a variety of artificial and real datasets.

**Citation**
We provided both iPynb illustrative code and Java production code for reproducible and experimental purposes under [LICENSE](https://github.com/hrluo/DistancePreservingMatrixSketch/blob/master/LICENSE).
Please cite our paper using following BibTeX item:

    @article{lel2020distancepreserving,
    title={A Distance-preserving Matrix Sketch},
    author={Leland Wilkinson and Hengrui Luo},
    year={2020},
    eprint={2009.03979},
    archivePrefix={arXiv},
    primaryClass={cs.HC}
	}

Thank you again for the interest and please reach out if you have further questions.
