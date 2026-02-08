import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../models/exam.dart';
import '../providers/exam_providers.dart';

class AddEditExamScreen extends ConsumerStatefulWidget {
  const AddEditExamScreen({super.key});

  @override
  ConsumerState<AddEditExamScreen> createState() => _AddEditExamScreenState();
}

class _AddEditExamScreenState extends ConsumerState<AddEditExamScreen> {
  final _formKey = GlobalKey<FormState>();

  // Form Fields
  String _title = '';
  String _description = '';
  DateTime _startTime = DateTime.now().add(const Duration(days: 1));
  DateTime _endTime = DateTime.now().add(const Duration(days: 1, hours: 2));
  int _durationMinutes = 120;

  // Questions
  List<Question> _questions = [];

  bool _isLoading = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Create New Exam')),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Form(
              key: _formKey,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  _buildBasicDetailsSection(),
                  const SizedBox(height: 24),
                  _buildQuestionsSection(),
                  const SizedBox(height: 32),
                  ElevatedButton(
                    onPressed: _submitForm,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                    ),
                    child: const Text('Create Exam'),
                  ),
                ],
              ),
            ),
    );
  }

  Widget _buildBasicDetailsSection() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Basic Details',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 16),
            TextFormField(
              decoration: const InputDecoration(labelText: 'Exam Title'),
              validator: (value) =>
                  (value == null || value.isEmpty) ? 'Title is required' : null,
              onSaved: (value) => _title = value!,
            ),
            const SizedBox(height: 16),
            TextFormField(
              decoration: const InputDecoration(labelText: 'Description'),
              maxLines: 3,
              onSaved: (value) => _description = value ?? '',
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: _DateTimePicker(
                    label: 'Start Time',
                    selectedDate: _startTime,
                    onDateSelected: (date) => setState(() => _startTime = date),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: _DateTimePicker(
                    label: 'End Time',
                    selectedDate: _endTime,
                    onDateSelected: (date) => setState(() => _endTime = date),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            TextFormField(
              initialValue: _durationMinutes.toString(),
              decoration: const InputDecoration(
                labelText: 'Duration (minutes)',
              ),
              keyboardType: TextInputType.number,
              validator: (value) {
                if (value == null || value.isEmpty) return 'Required';
                if (int.tryParse(value) == null) return 'Must be a number';
                return null;
              },
              onSaved: (value) => _durationMinutes = int.parse(value!),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildQuestionsSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Questions (${_questions.length})',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            Text(
              'Total Marks: ${_calculateTotalMarks()}',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (_questions.isEmpty)
          const Card(
            child: Padding(
              padding: EdgeInsets.all(32),
              child: Center(child: Text('No questions added yet')),
            ),
          )
        else
          ..._questions.asMap().entries.map(
            (entry) => _buildQuestionCard(entry.key, entry.value),
          ),

        const SizedBox(height: 16),
        Center(
          child: ElevatedButton.icon(
            onPressed: _addQuestion,
            icon: const Icon(Icons.add),
            label: const Text('Add Question'),
          ),
        ),
      ],
    );
  }

  Widget _buildQuestionCard(int index, Question question) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: CircleAvatar(child: Text('${index + 1}')),
        title: Text(
          question.text,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Text('${question.type.name} • ${question.marks} marks'),
        trailing: IconButton(
          icon: const Icon(Icons.delete, color: Colors.red),
          onPressed: () {
            setState(() {
              _questions.removeAt(index);
            });
          },
        ),
        onTap: () => _editQuestion(index, question),
      ),
    );
  }

  int _calculateTotalMarks() {
    return _questions.fold(0, (sum, q) => sum + q.marks);
  }

  void _addQuestion() async {
    final Question? result = await showDialog<Question>(
      context: context,
      builder: (context) => const QuestionDialog(),
    );

    if (result != null) {
      setState(() {
        // Set order number based on list position
        final newQuestion = Question(
          text: result.text,
          type: result.type,
          options: result.options,
          correctAnswer: result.correctAnswer,
          marks: result.marks,
          orderNumber: _questions.length + 1,
        );
        _questions.add(newQuestion);
      });
    }
  }

  void _editQuestion(int index, Question question) async {
    // For now just delete and re-add or implement edit logic
    // Implementing simple edit by pre-filling
    final Question? result = await showDialog<Question>(
      context: context,
      builder: (context) => QuestionDialog(initialQuestion: question),
    );

    if (result != null) {
      setState(() {
        _questions[index] = Question(
          text: result.text,
          type: result.type,
          options: result.options,
          correctAnswer: result.correctAnswer,
          marks: result.marks,
          orderNumber: question.orderNumber, // Keep original order
        );
      });
    }
  }

  Future<void> _submitForm() async {
    if (!_formKey.currentState!.validate()) return;

    if (_questions.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please add at least one question')),
      );
      return;
    }

    _formKey.currentState!.save();

    setState(() => _isLoading = true);

    try {
      final exam = Exam(
        title: _title,
        description: _description,
        startTime: _startTime,
        endTime: _endTime,
        durationMinutes: _durationMinutes,
        totalMarks: _calculateTotalMarks(),
        questions: _questions,
        status: ExamStatus.DRAFT,
      );

      await ref.read(examServiceProvider).createExam(exam);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Exam created successfully!')),
        );
        Navigator.pop(context, true); // Return true to trigger refresh
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('Error creating exam: $e')));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }
}

class _DateTimePicker extends StatelessWidget {
  final String label;
  final DateTime selectedDate;
  final Function(DateTime) onDateSelected;

  const _DateTimePicker({
    required this.label,
    required this.selectedDate,
    required this.onDateSelected,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        const SizedBox(height: 4),
        InkWell(
          onTap: () async {
            final date = await showDatePicker(
              context: context,
              initialDate: selectedDate,
              firstDate: DateTime.now(),
              lastDate: DateTime.now().add(const Duration(days: 365)),
            );
            if (date != null && context.mounted) {
              final time = await showTimePicker(
                context: context,
                initialTime: TimeOfDay.fromDateTime(selectedDate),
              );
              if (time != null) {
                onDateSelected(
                  DateTime(
                    date.year,
                    date.month,
                    date.day,
                    time.hour,
                    time.minute,
                  ),
                );
              }
            }
          },
          child: Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey),
              borderRadius: BorderRadius.circular(4),
            ),
            child: Row(
              children: [
                const Icon(Icons.calendar_today, size: 16),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    DateFormat('MMM d, h:mm a').format(selectedDate),
                    style: const TextStyle(fontSize: 14),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class QuestionDialog extends StatefulWidget {
  final Question? initialQuestion;

  const QuestionDialog({super.key, this.initialQuestion});

  @override
  State<QuestionDialog> createState() => _QuestionDialogState();
}

class _QuestionDialogState extends State<QuestionDialog> {
  final _formKey = GlobalKey<FormState>();

  late String _text;
  late QuestionType _type;
  late int _marks;
  late String _correctAnswer;
  final List<String> _options = [];
  final TextEditingController _optionController = TextEditingController();

  @override
  void initState() {
    super.initState();
    if (widget.initialQuestion != null) {
      _text = widget.initialQuestion!.text;
      _type = widget.initialQuestion!.type;
      _marks = widget.initialQuestion!.marks;
      _correctAnswer = widget.initialQuestion!.correctAnswer;
      _options.addAll(widget.initialQuestion!.options);
    } else {
      _text = '';
      _type = QuestionType.MULTIPLE_CHOICE;
      _marks = 10;
      _correctAnswer = '';
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(
        widget.initialQuestion == null ? 'Add Question' : 'Edit Question',
      ),
      content: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<QuestionType>(
                value: _type,
                decoration: const InputDecoration(labelText: 'Type'),
                items: QuestionType.values
                    .map(
                      (t) => DropdownMenuItem(
                        value: t,
                        child: Text(t.name.replaceAll('_', ' ')),
                      ),
                    )
                    .toList(),
                onChanged: (val) {
                  setState(() {
                    _type = val!;
                    // Reset options if switching type
                    if (_type == QuestionType.TRUE_FALSE) {
                      _options.clear();
                      _options.addAll(['True', 'False']);
                      _correctAnswer = 'True';
                    } else if (_type == QuestionType.SHORT_ANSWER) {
                      _options.clear();
                    }
                  });
                },
              ),
              const SizedBox(height: 12),
              TextFormField(
                initialValue: _text,
                decoration: const InputDecoration(labelText: 'Question Text'),
                maxLines: 2,
                validator: (v) => v!.isEmpty ? 'Required' : null,
                onSaved: (v) => _text = v!,
              ),
              const SizedBox(height: 12),
              TextFormField(
                initialValue: _marks.toString(),
                decoration: const InputDecoration(labelText: 'Marks'),
                keyboardType: TextInputType.number,
                validator: (v) => v!.isEmpty ? 'Required' : null,
                onSaved: (v) => _marks = int.parse(v!),
              ),
              const SizedBox(height: 12),

              if (_type == QuestionType.MULTIPLE_CHOICE) ...[
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _optionController,
                        decoration: const InputDecoration(
                          labelText: 'New Option',
                        ),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.add),
                      onPressed: () {
                        if (_optionController.text.isNotEmpty) {
                          setState(() {
                            _options.add(_optionController.text);
                            if (_correctAnswer.isEmpty) {
                              _correctAnswer = _optionController.text;
                            }
                            _optionController.clear();
                          });
                        }
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 8,
                  children: _options
                      .map(
                        (opt) => Chip(
                          label: Text(opt),
                          onDeleted: () {
                            setState(() {
                              _options.remove(opt);
                              if (_correctAnswer == opt) {
                                _correctAnswer = _options.isNotEmpty
                                    ? _options.first
                                    : '';
                              }
                            });
                          },
                        ),
                      )
                      .toList(),
                ),
              ],

              const SizedBox(height: 12),
              if (_type != QuestionType.SHORT_ANSWER)
                DropdownButtonFormField<String>(
                  value:
                      _correctAnswer.isNotEmpty &&
                          _options.contains(_correctAnswer)
                      ? _correctAnswer
                      : null,
                  decoration: const InputDecoration(
                    labelText: 'Correct Answer',
                  ),
                  items: _options
                      .map(
                        (opt) => DropdownMenuItem(value: opt, child: Text(opt)),
                      )
                      .toList(),
                  onChanged: (val) => setState(() => _correctAnswer = val!),
                  validator: (v) => v == null ? 'Required' : null,
                  onSaved: (v) => _correctAnswer = v!,
                )
              else
                TextFormField(
                  initialValue: _correctAnswer,
                  decoration: const InputDecoration(
                    labelText: 'Correct Answer Keyword',
                  ),
                  validator: (v) => v!.isEmpty ? 'Required' : null,
                  onSaved: (v) => _correctAnswer = v!,
                ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel'),
        ),
        ElevatedButton(
          onPressed: () {
            if (_formKey.currentState!.validate()) {
              _formKey.currentState!.save();
              if (_type == QuestionType.MULTIPLE_CHOICE &&
                  _options.length < 2) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Add at least 2 options')),
                );
                return;
              }
              Navigator.pop(
                context,
                Question(
                  text: _text,
                  type: _type,
                  options: _options,
                  correctAnswer: _correctAnswer,
                  marks: _marks,
                  orderNumber: 0, // Will be set by parent
                ),
              );
            }
          },
          child: const Text('Save'),
        ),
      ],
    );
  }
}
