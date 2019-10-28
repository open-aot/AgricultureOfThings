# Evaluation, Data Preprocessing and Representation
import pandas as pd
import numpy as np
import json
from sklearn.base import clone
from statistics import mean
from sklearn.metrics import mean_absolute_error, make_scorer
from sklearn.model_selection import train_test_split, cross_val_score
import pprint
from matplotlib import cm
import matplotlib.pyplot as plt
import matplotlib
from mpl_toolkits.mplot3d import Axes3D
# Machine Learning Models
from sklearn.tree import DecisionTreeRegressor
from sklearn.svm import SVC, SVR
from sklearn.linear_model import LogisticRegression, Ridge
from sklearn.ensemble import RandomForestRegressor

pp = pprint.PrettyPrinter(indent=4)  # Pretty Printers
pd.options.mode.chained_assignment = None  # Silence warnings


def splitData(df, test_size=0.2):
    # Separate features from labels from training/test set
    train, test = train_test_split(df, test_size=test_size, shuffle=True)
    X = train.iloc[:, :-1]
    y = train.iloc[:, -1]
    test_features = test.iloc[:, :-1]
    test_labels = test.iloc[:, -1]
    return X, y, test_features, test_labels


############################# - PREPROCESSING - #############################
################################# - START - #################################
# READ THE CSV FILE
with open("openAgriculture/Input/sensor2.json") as f:
    data = json.load(f)
# Define column names
df_base = pd.DataFrame(
    data, columns=["Timestamp", "Moisture", "Humidity", "Temperature"])
# Remove timestamp from features and append moisture in the end of the df
df_base.pop('Timestamp')
target = df_base.pop('Moisture')
df_base['Moisture'] = target
print('Basic Dataset:')
print(df_base)


# APPEND META-FEATURES IN DATASET
# Drop all rows that contain at least one nan value
df = df_base.dropna(how='any')
target = df.pop('Moisture')
# Add some meta-features in the Feature Space
df['minHumTemp'] = np.minimum(df['Humidity'], df['Temperature'])
df['maxHumTemp'] = np.maximum(df['Humidity'], df['Temperature'])
df['expTemp'] = np.exp(df['Temperature'])
# df['expHum'] = np.exp(df['Humidity'])
df['logTemp'] = np.log(df['Temperature'])
df['logHum'] = np.log(df['Humidity'])
df['hum:temp'] = df['Humidity']/df['Temperature']
df['temp:hum'] = df['Temperature']/df['Humidity']
df['temp*hum'] = df['Temperature']*df['Humidity']
# Append Class Variable in the end
df['Moisture'] = target.astype(int)
print('Dataset after meta features addition:')
print(df)


# REMOVE CORRELATED FEATURES FROM DATASET
updatedDataset = df
# Create correlation matrix for the current dataset
corr_matrix = updatedDataset.corr('pearson').abs()
print("Correlation matrix of current dataset:")
print(corr_matrix)
# Select upper triangle of correlation matrix
upper = corr_matrix.where(
    np.triu(np.ones(corr_matrix.shape), k=1).astype(np.bool))
# Find index of feature columns with correlation greater than 0.90
to_drop = [column for column in upper.columns if any(upper[column] > 0.90)]
print('Features to drop:', end='')
print(to_drop)
# Drop correlated unnecessary features
finalDataset = updatedDataset.drop(updatedDataset[to_drop], axis=1)
print('Dataset after correlated features removal:')
print(finalDataset)
############################# - PREPROCESSING - #############################
################################## - END - ##################################


################################ - START - #################################
############################# - CRAFT MODELS - #############################
# Models to Train
ml_models = [
    LogisticRegression(multi_class='ovr',
                       solver='liblinear',
                       class_weight='balanced'),
    LogisticRegression(multi_class='multinomial',
                       solver='lbfgs',
                       class_weight='balanced', max_iter=10000),
    DecisionTreeRegressor(min_samples_split=5),
    RandomForestRegressor(n_estimators=100),
    SVC(kernel='rbf', gamma="auto"),
    SVC(kernel='poly', gamma="auto"),
    SVC(kernel='sigmoid', gamma="auto"),
    SVR(gamma="auto"),
    Ridge(alpha=1.0)
]
############################## - CRAFT MODELS - #############################
################################## - END - ##################################


################################ - START - #################################
############################## - EVALUATION - ##############################
MAE = make_scorer(mean_absolute_error)

# Run many iterations on each model
modelsScoresDict_reg = {"Ridge": [],
                        "LogisticRegression": [],
                        "DecisionTreeRegressor": [],
                        "RandomForestRegressor": [],
                        "SVC": [],
                        "SVR": []}
iterations = 10
for i in range(0, iterations):
    # Split data in each iteration
    X, y, test_features, test_labels = splitData(finalDataset)
    print(i)
    # Train all models in each iteration and collect the avg. cv scores
    for model in ml_models:
        score = cross_val_score(
            model, X, y, cv=5, scoring=MAE)
        score = score.mean()
        # Append the current score in the respective dictionary
        modelsScoresDict_reg[str(model).split("(")[0]].append(score)

# Print models' performances
meanPerformanceTuples = []
for x in modelsScoresDict_reg.keys():
    meanPerformanceTuples.append([x, mean(modelsScoresDict_reg[x])])
    print(x, mean(modelsScoresDict_reg[x]))

# Random Forest Regressor
print("Best Model after evaluation: ", end='')
print(min(meanPerformanceTuples, key=lambda x: x[1])[0])
################################# - END - ##################################
############################## - EVALUATION - ##############################

################################ - START - #################################
############################ - INTERPRETATION - ############################
# Print feature importances and score in test data
X, y, test_features, test_labels = splitData(finalDataset, 0.2)
modelFitted = RandomForestRegressor(n_estimators=100).fit(X, y)
# Collect feature importances of best model collect from evaluation section
print("Features: ", end='')
print(list(finalDataset.iloc[:, :-1].columns))
print("Feature Importances: ", end='')
print(modelFitted.feature_importances_)

# Score of best model on test set
print("Score on test set: ", end='')
print(mean_absolute_error(modelFitted.predict(test_features), test_labels))
################################# - END - ##################################
############################ - INTERPRETATION - ############################

################################ - START - #################################
############################ - REPRESENTATION - ############################
# Plot Data in 3D space
threedee = plt.figure().gca(projection='3d')
sc = threedee.scatter(finalDataset['Humidity'],
                      finalDataset['Temperature'],
                      finalDataset['Moisture'], s=30, c=finalDataset['Moisture'], cmap=cm.RdBu)
threedee.set_xlabel('Humidity')
threedee.set_ylabel('Temperature')
threedee.set_zlabel('Moisture')
plt.colorbar(sc)
plt.show()
################################# - END - ##################################
############################ - REPRESENTATION - ############################
