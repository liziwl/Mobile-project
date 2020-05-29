import pandas as pd 
import numpy as np 
import os

class One_auth:
    X = None
    weight = None
    mean_distance = None
    delta_distance = None

    def extract_fft(self,df):
        # Normalization
        normalized_df=(df-df.mean())/df.std()
        z_values = normalized_df.z.to_numpy()

        # alignment
        base_signal = np.zeros(100)
        base_signal[0:50]=1
        base_signal[50:100]=-1
        co = np.correlate(z_values,base_signal,mode='same')
        max_index = np.argmax(co)
        segment = normalized_df.shift(periods=256-max_index,fill_value=0).z.to_numpy()

        # FFT
        fft_z =  np.fft.fft(segment,n=512)
        abs_fft_z = np.abs(fft_z)

        return abs_fft_z[0:256]
    
    def cal_average_distance(self,X):
        N = len(X)
        D = 0
        for i in range(N-1):
            for j in range(i+1,N):
                D += np.sqrt(np.sum(np.square(X[i]-X[j])))
        C_n_2 = N*(N-1)/2
        return D/C_n_2

    def fit(self,train_data_dir):
        # 读取文件
        features=[]
        for root, dirs, files in os.walk(train_data_dir):
            for filename in files:
                fullname = os.path.join(root,filename)
                if filename.endswith('.csv'):
                    df = pd.read_csv(fullname,names=['x','y','z'])
                    # plt.subplot(5,6,i)
                    # i+=1
                    # normalized_df=(df-df.mean())/df.std()
                    # normalized_df.z.plot()
                    feature = self.extract_fft(df)
                    features.append(feature)
        # 转换特征
        raw_X = np.array(features)
        variance = np.var(raw_X,axis=0)
        self.weight = (max(variance)-variance)/np.sum(max(variance)-variance)
        self.X = raw_X*self.weight
        # 计算距离和阈值
        distances=[]
        for i in range(2,len(self.X)):
            distances.append(self.cal_average_distance(self.X[0:i,:]))
        self.delta_distance = np.std(distances[5:len(self.X)])
        # self.mean_distance = np.mean(distances[10:30])
        self.mean_distance = distances[-1]

    def predict(self,x_new):
        new_sample_feature = self.extract_fft(x_new)*self.weight
        D2 = self.cal_average_distance(np.append(self.X,new_sample_feature.reshape(1,-1),axis=0))
        if(D2-self.mean_distance > 2*self.delta_distance):
            return False
        else:
            return True