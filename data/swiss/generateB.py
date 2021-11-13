from numpy import genfromtxt
my_data = genfromtxt('swissB.csv', delimiter=',')
my_data = my_data[1:my_data.shape[0],:] 
print(my_data.shape)
print(my_data)

import numpy as np
from sklearn import manifold, datasets
import matplotlib.pyplot as plt

n_components = 2
n_columns = 3#how many columns are involved in this run of tSNE?
for perp in [5,10,50,100]:
    for n_columns in [3,50,100]:
        tsne = manifold.TSNE(
                n_components=2,\
                #Dimension of the embedded space is always 2 for visualization purpose.
                init="random",\
                random_state=0,\
                perplexity=perp,\
                verbose=1)
        my_tsne = tsne.fit_transform(my_data[:,0:n_columns])

        np.savetxt("swissB_"+str(n_columns)+'_perp_'+str(perp)+'_tsne'+".csv", my_data, delimiter=",")
        
        plt.figure(figsize=(6,6))
        plt.scatter(my_data[:,0],my_data[:,1],c='b',label='original')
        plt.scatter(my_tsne[:,0],my_tsne[:,1],c='r',label='tSNE')
        plt.title('n_columns='+str(n_columns)+'\n perplexity='+str(perp))
        plt.legend()
        plt.savefig("swiss_"+str(n_columns)+'_perp_'+str(perp)+'_tsne'+".png")
