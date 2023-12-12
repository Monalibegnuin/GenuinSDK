package com.begenuin.library.core.interfaces;


import com.begenuin.library.data.model.QuestionModel;

import java.util.ArrayList;

public interface QuestionViewModelListener {
    void onQuestionsSyncSuccess(ArrayList<QuestionModel> questionsList, boolean isShowQuestion);

    void onQuestionsSyncFailure();
}
