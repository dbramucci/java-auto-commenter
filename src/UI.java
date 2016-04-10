interface UI {
    /**promptInput
     *
     * prompts the user for input and returns their answer
     *
     * @param prompt the prompt to display to the user
     * @return the users input
     */
    String promptInput(String prompt);

    /**getUserFileName
     * displays a file selection dialog and returns and returns the base file name  as a string
     *
     * @return the base file name as a string string
     */
    String getUserFileName();

    /**display
     *
     * displays the provided string to the user
     *
     * @param output string to display to the user
     */
    void display(String output);
}
