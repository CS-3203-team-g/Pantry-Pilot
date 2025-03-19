// Nutrition Facts Management
const RecipeDataNutrition = {
    getNutritionFacts() {
        return RecipeDataCore.draftRecipe ? 
            RecipeDataCore.draftNutritionFacts : (RecipeDataCore.data.nutritionFacts || []);
    },
    
    addOrUpdateNutritionFact(ingredientID, unitID, nutritionData) {
        if (!RecipeDataCore.data.nutritionFacts) {
            RecipeDataCore.data.nutritionFacts = [];
        }
        
        const parsedIngredientID = parseInt(ingredientID);
        const parsedUnitID = parseInt(unitID);
        
        if (RecipeDataCore.draftRecipe) {
            let existingIndices = RecipeDataCore.draftNutritionFacts
                .map((nf, index) => nf.ingredientID === parsedIngredientID ? index : -1)
                .filter(index => index !== -1);
            
            const nutritionFact = {
                ingredientID: parsedIngredientID,
                unitID: parsedUnitID,
                ...nutritionData
            };
            
            if (existingIndices.length > 0) {
                existingIndices.forEach(index => {
                    RecipeDataCore.draftNutritionFacts[index] = {
                        ...RecipeDataCore.draftNutritionFacts[index],
                        ...nutritionData
                    };
                });
                
                const hasExactUnitMatch = RecipeDataCore.draftNutritionFacts.some(nf => 
                    nf.ingredientID === parsedIngredientID && nf.unitID === parsedUnitID
                );
                
                if (!hasExactUnitMatch) {
                    RecipeDataCore.draftNutritionFacts.push(nutritionFact);
                }
            } else {
                RecipeDataCore.draftNutritionFacts.push(nutritionFact);
            }
        } else {
            let existingIndices = RecipeDataCore.data.nutritionFacts
                .map((nf, index) => nf.ingredientID === parsedIngredientID ? index : -1)
                .filter(index => index !== -1);
            
            const nutritionFact = {
                ingredientID: parsedIngredientID,
                unitID: parsedUnitID,
                ...nutritionData
            };
            
            if (existingIndices.length > 0) {
                existingIndices.forEach(index => {
                    RecipeDataCore.data.nutritionFacts[index] = {
                        ...RecipeDataCore.data.nutritionFacts[index],
                        ...nutritionData
                    };
                });
                
                const hasExactUnitMatch = RecipeDataCore.data.nutritionFacts.some(nf => 
                    nf.ingredientID === parsedIngredientID && nf.unitID === parsedUnitID
                );
                
                if (!hasExactUnitMatch) {
                    RecipeDataCore.data.nutritionFacts.push(nutritionFact);
                }
            } else {
                RecipeDataCore.data.nutritionFacts.push(nutritionFact);
            }
            
            RecipeDataCore.updateJsonEditor();
        }
    },
    
    getNutritionFact(ingredientID, unitID) {
        ingredientID = parseInt(ingredientID);
        unitID = parseInt(unitID);
        
        const nutritionFacts = this.getNutritionFacts();
        
        let nutritionFact = nutritionFacts.find(nf => 
            nf.ingredientID === ingredientID && 
            nf.unitID === unitID
        );
        
        if (!nutritionFact) {
            nutritionFact = nutritionFacts.find(nf => 
                nf.ingredientID === ingredientID
            );
        }
        
        return nutritionFact;
    },

    syncDraftNutritionFacts() {
        if (RecipeDataCore.draftNutritionFacts.length === 0) return;
        
        RecipeDataCore.draftNutritionFacts.forEach(draftNF => {
            const existingIndex = RecipeDataCore.data.nutritionFacts.findIndex(nf => 
                nf.ingredientID === draftNF.ingredientID && 
                nf.unitID === draftNF.unitID
            );
            
            if (existingIndex >= 0) {
                RecipeDataCore.data.nutritionFacts[existingIndex] = draftNF;
            } else {
                RecipeDataCore.data.nutritionFacts.push(draftNF);
            }
        });
    }
};

window.RecipeDataNutrition = RecipeDataNutrition;